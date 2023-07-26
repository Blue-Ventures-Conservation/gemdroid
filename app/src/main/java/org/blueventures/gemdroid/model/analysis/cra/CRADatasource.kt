package org.blueventures.gemdroid.model.analysis.cra

import android.net.Uri
import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.apiResultCheck
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.iryndin.jdbf.core.DbfFieldTypeEnum
import net.iryndin.jdbf.reader.DbfReader
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.cra.CRAKey
import org.blueventures.gemdroid.data.analysis.cra.Shapefile
import org.blueventures.gemdroid.data.analysis.cra.Success
import org.blueventures.gemdroid.data.analysis.cra.UploadName
import org.blueventures.gemdroid.model.SignIn
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.resultCheck
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CRADatasource(
    private val storage: FirebaseStorage = Firebase.storage,
    private val api: Api.Service = Api.Service.instance(),
    private val auth: FirebaseAuth = Firebase.auth,
): ApiDatasource(api, auth) {
    suspend fun getRemoteCRAs(): Result<List<String>> = suspendCoroutine { cont ->
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/shps").listAll()
                .addOnSuccessListener { result ->
                    val files = mutableListOf<String>()
                    result.items.forEach {
                        if (it.name.endsWith(".json")) {
                            files.add(it.name.substringBeforeLast("."))
                        }
                    }
                    cont.resume(Result.success(files))
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(NoStack(R.string.could_not_reach_storage)))
                }
        } ?: run {
            cont.resume(Result.failure(SignIn.not))
        }
    }

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?): Result<CRAFile> {
        val craDirRes = FileService.createDir(roiDir, crasDir)
        if (craDirRes.isFailure) {
            return Result.failure(craDirRes.exceptionOrNull()!!)
        }

        val crasDir = craDirRes.getOrNull()!!

        val unzipDirRes = FileService.createDir(crasDir, crasUnzipDir)
        if (unzipDirRes.isFailure) {
            return Result.failure(unzipDirRes.exceptionOrNull()!!)
        }

        val unzipDir = unzipDirRes.getOrNull()!!

        val result = when {
            files.isEmpty() -> Result.failure(NoStack(R.string.no_file_selected))
            files.size == 1 -> validateShapes(crasDir, remoteCRAs, previous, unzip(unzipDir, files[0], names[0]))
            else -> validateShapes(crasDir, remoteCRAs, previous, copyShapes(unzipDir, files, names))
        }

        FileService.deleteDir(unzipDir)

        return result
    }

    private fun copyShapes(dir: File, shps: List<InputStream?>, names: List<String?>): Result<List<String>> {
        val paths = mutableListOf<String>()
        shps.forEachIndexed { i, shp ->
            if (shp == null || names[i] == null) {
                return Result.failure(NoStack(R.string.could_not_read_shps))
            }
            val path = File(dir, names[i]!!).path
            val streamRes = FileService.streamToFile(shp, path)
            if (streamRes.isFailure) {
                return Result.failure(streamRes.exceptionOrNull()!!)
            }

            paths.add(path)
        }

        return Result.success(paths)
    }

    private fun unzip(dir: File, zip: InputStream?, name: String?): Result<List<String>> {
        val notZip = NoStack(R.string.extract_must_be_zip)
        if (name?.substringAfterLast(".")?.lowercase() != "zip") {
            return Result.failure(notZip)
        }

        if (zip == null) {
            return Result.failure(NoStack(R.string.could_not_open_zip))
        }

        val pathsRes = FileService.unzip(zip, dir.path)
        if (pathsRes.isFailure) {
            return Result.failure(pathsRes.exceptionOrNull()!!)
        }
        return Result.success(pathsRes.getOrNull()!!)
    }

    private val assetRegex by lazy { Regex("[a-zA-Z\\d\\-_]+") }
    private fun validateShapes(crasDir: File, remoteCRAs: List<String>, previous: String?, pathsResult: Result<List<String>>): Result<CRAFile> {
        if (pathsResult.isFailure) {
            return Result.failure(pathsResult.exceptionOrNull()!!)
        }

        val paths = pathsResult.getOrNull()!!

        val badShape = NoStack(R.string.shp_missing_files)
        if (paths.size < 4) {
            return Result.failure(badShape)
        }

        var shp: String? = null
        var shpName = "shp"
        var shx: String? = null
        var shxName = "shx"
        var dbf: String? = null
        var dbfName = "dbf"
        var prj: String? = null
        var prjName = "prj"

        paths.forEach { path ->
            when(path.substringAfterLast(".").lowercase()) {
                "shp" -> {
                    shp = path
                    shpName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                "shx" -> {
                    shx = path
                    shxName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                "dbf" -> {
                    dbf = path
                    dbfName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                "prj" -> {
                    prj = path
                    prjName = path.substringAfterLast(FileService.sep).substringBeforeLast(".")
                }
                else -> {
                    return Result.failure(badShape)
                }
            }
        }

        if (shp == null || shx == null || dbf == null || prj == null) {
            return Result.failure(badShape)
        }

        if (shpName != shxName || shpName != dbfName || shpName != prjName) {
            return Result.failure(NoStack(R.string.shps_names_must_match))
        }

        if (previous != null && previous == shpName) {
            return Result.failure(NoStack(R.string.shps_must_differ))
        }

        if (remoteCRAs.contains(shpName)) {
            return Result.failure(NoStack(R.string.please_reuse_shp))
        }

        val numerics = mutableListOf<String>()
        val strings = mutableListOf<String>()
        val numericsMap = mutableMapOf<String, MutableMap<String, Int>>()
        val stringsMap = mutableMapOf<String, MutableMap<String, Int>>()

        try {
            // these constructors will inspect the file header
            val shpStream = FileInputStream(shp)
            val sr = ShapeFileReader(shpStream)
            val dr = DbfReader(FileInputStream(dbf))

            var count = 0
            var s = sr.next()
            while(s != null) {
                count++

                val rec = dr.read()
                rec.fields.forEach { field ->
                    val name = field.name
                    when(field.type) {
                        DbfFieldTypeEnum.Numeric -> {
                            addToMap(numericsMap, name, rec.getString(name).toFloat().toInt().toString())
                        }
                        DbfFieldTypeEnum.Character -> {
                            addToMap(stringsMap, name, rec.getString(name))
                        }
                        else -> {}
                    }
                }

                s = sr.next()
            }
            shpStream.close()
            dr.close()

            numericsMap.forEach { (nf, nc) ->
                if (nc.size < count) {
                    stringsMap.forEach { (sf, sc) ->
                        if (nc.size == sc.size) {
                            numerics.add(nf)
                            strings.add(sf)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (numerics.size <= 0) {
            return Result.failure(NoStack(R.string.shp_no_candidate_num))
        }

        if (strings.size <= 0) {
            return Result.failure(NoStack(R.string.shp_no_candidate_char))
        }

        if (!assetRegex.matches(shpName)) {
            return Result.failure(NoStack(R.string.shp_name_alphanumeric))
        }

        val zipFile = File(crasDir, "$shpName.zip")

        val zipRes = FileService.zip(arrayOf(shp!!, shx!!, dbf!!, prj!!), zipFile.path)
        if (zipRes.isFailure) {
            return Result.failure(zipRes.exceptionOrNull()!!)
        }

        val stringValues = mutableMapOf<String, List<String>>()
        stringsMap.forEach { (k, v) ->
            stringValues[k] = v.keys.toList()
        }

        return Result.success(CRAFile(
            localFile = zipFile,
            fields = Fields(numerics, strings, stringValues))
        )
    }

    private fun addToMap(m: MutableMap<String, MutableMap<String, Int>>, field: String, value: String) {
        var fieldMap = m[field]
        if (fieldMap == null) {
            fieldMap = mutableMapOf()
            m[field] = fieldMap
        }
        val count = fieldMap[value]
        if (count == null) {
            fieldMap[value] = 1
        } else {
            fieldMap[value] = count + 1
        }
    }

    suspend fun getCRAFields(cont: CRAFile, hist: CRAFile?): Result<Fields> {
        if (hist == null || cont.equivalent(hist)) return craFields(cont)
        val contRes = craFields(cont)
        val histRes = craFields(hist)
        val err = resultCheck(contRes, histRes)
        if (err != null) return Result.failure(err)
        return mergeFields(contRes.getOrNull()!!, histRes.getOrNull()!!)
    }

    private fun mergeFields(f1: Fields, f2: Fields): Result<Fields> {
        val mismatch = NoStack(R.string.shps_must_match_fields)
        return when {
            f1.complete() && f2.complete() -> {
                if (f1.chosenNumeric == f2.chosenNumeric && f1.chosenString == f2.chosenString && f1.chosenStringValues == f2.chosenStringValues) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f1.complete() && !f2.complete() -> {
                if (f2.numerics!!.contains(f1.chosenNumeric) && f2.strings!!.contains(f1.chosenString) && f2.stringValues!![f1.chosenString] == f1.chosenStringValues) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f2.complete() && !f1.complete() -> {
                if (f1.numerics!!.contains(f2.chosenNumeric) && f1.strings!!.contains(f2.chosenString) && f1.stringValues!![f2.chosenString] == f2.chosenStringValues) {
                    Result.success(f2)
                } else {
                    Result.failure(mismatch)
                }
            }
            !f1.complete() && !f2.complete() -> {
                val n1 = f1.numerics!!
                val n2 = f2.numerics!!
                val s1 = f1.strings!!
                val s2 = f2.strings!!
                val sv1 = f1.stringValues!!
                val sv2 = f2.stringValues!!
                if (s1 == s2 && n1 == n2 && sv1 == sv2) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            else -> {
                // should not be reachable
                Result.failure(NoStack(R.string.unreachable_err))
            }
        }
    }

    private suspend fun craFields(cra: CRAFile): Result<Fields> {
        if (cra.fields.parsedLocally() || cra.fields.complete()) return Result.success(cra.fields)
        if (cra.storageKey == null) return Result.failure(NoStack(R.string.internal_storage_key_err))
        if (auth.currentUser?.uid == null) return Result.failure(SignIn.not)
        val uid = auth.currentUser!!.uid
        return try {
            fetchFields(cra, cra.storageKey, uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(IOException::class)
    private suspend fun fetchFields(cra: CRAFile, key: String, uid: String): Result<Fields> = suspendCoroutine { cont ->
        val tmp = File.createTempFile("cras", "json")
        tmp.deleteOnExit()
        storage.reference.child("users/$uid/shps/$key.json").getFile(tmp).addOnSuccessListener {
            val shpRes = Shapefile.fromFile(tmp)
            if (shpRes.isFailure) {
                cont.resume(Result.failure(shpRes.exceptionOrNull()!!))
            } else {
                val shp = shpRes.getOrNull()!!
                cra.eeUploadName = shp.tableUploadOperationName
                cont.resume(
                    Result.success(Fields(
                        chosenNumeric = shp.numericClassField,
                        chosenString = shp.stringClassField,
                        chosenStringValues = shp.stringClassValues,
                    ))
                )
            }
        }.addOnFailureListener {
            cont.resume(Result.failure(it))
        }
    }

    suspend fun uploadCRAs(c1: CRAFile, c2: CRAFile): Result<Unit> {
        val r1 = uploadCRA(c1)
        val r2 = uploadCRA(c2)

        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)
        return Result.success(Unit)
    }

    suspend fun uploadCRA(cra: CRAFile): Result<Unit> {
        if (cra.localFile == null || cra.fields.chosenNumeric == null || cra.fields.chosenString == null) {
            return Result.failure(NoStack(R.string.internal_sho_err))
        }

        if (auth.currentUser?.uid == null) return Result.failure(SignIn.not)
        val uid = auth.currentUser!!.uid
        val key = cra.key()
        val zip = cra.localFile
        val result = uploadShapefile(key, uid, zip)

        FileService.deleteFile(zip)

        return result
    }

    private suspend fun uploadShapefile(key: String, uid: String, zip: File): Result<Unit> = suspendCoroutine { cont ->
        storage.reference.child("users/$uid/shps/$key.zip").putFile(Uri.fromFile(zip))
            .addOnSuccessListener {
                cont.resume(Result.success(Unit))
            }.addOnFailureListener {
                cont.resume(Result.failure(it))
            }
    }

    suspend fun ingestCRAs(c1: CRAFile, c2: CRAFile): Result<Unit> {
        val r1 = ingestCRA(c1)
        val r2 = ingestCRA(c2)
        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)
        return Result.success(Unit)
    }

    suspend fun ingestCRA(cra: CRAFile): Result<Unit> {
        val key = cra.key()
        val needed = ingestNeeded(cra.eeUploadName, key)
        if (needed.isFailure) return Result.failure(needed.exceptionOrNull()!!)
        if (!needed.getOrNull()!!) return Result.success(Unit)
        val result = api.ingestCRA(CRAKey(key))
        val err = apiResultCheck(result)
        if (err != null) return Result.failure(err)
        val data = result.data!!
        if (!data.success) return Result.failure(NoStack(R.string.gee_ingestion_falied))
        cra.eeUploadName = data.name
        return Result.success(Unit)
    }

    private suspend fun ingestNeeded(name: String?, key: String): Result<Boolean> {
        if (name == null || name == "") return Result.success(true)
        val result = api.awaitCRAUpload(UploadName(name, key))
        val err = apiResultCheck(result)
        if (err != null) return Result.failure(err)
        return Result.success(result.data!!.ingestNeeded())
    }

    suspend fun uploadFields(c1: CRAFile, c2: CRAFile): Result<Unit> {
        val r1 = uploadFields(c1)
        val r2 = uploadFields(c2)
        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)
        return Result.success(Unit)
    }

    suspend fun uploadFields(cra: CRAFile): Result<Unit> {
        if (auth.currentUser?.uid == null) return Result.failure(SignIn.not)
        val uid = auth.currentUser!!.uid
        return try {
            uploadFields(cra, uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(IOException::class)
    private suspend fun uploadFields(cra: CRAFile, uid: String): Result<Unit>  = suspendCoroutine { cont ->
        val key = cra.key()
        val tmp = File.createTempFile(key, "json")
        tmp.deleteOnExit()
        val shpRes = Shapefile.toFile(tmp, Shapefile(key, cra.eeUploadName!!, cra.fields.chosenNumeric!!, cra.fields.chosenString!!, cra.fields.chosenStringValues!!))

        if (shpRes.isFailure) {
            cont.resume(shpRes)
        } else {
            storage.reference.child("users/$uid/shps/$key.json").putFile(Uri.fromFile(tmp))
                .addOnSuccessListener {
                    cont.resume(Result.success(Unit))
                }.addOnFailureListener {
                    cont.resume(Result.failure(it))
                }
        }
    }

    fun saveCRAs(roiDir: File, cra: CRA): Result<Unit> {
        return CRA.toFile(File(File(roiDir, crasDir), crasFile), cra)
    }

    fun loadCRAs(roiDir: File) = CRA.fromFile(File(File(roiDir, crasDir), crasFile))

    fun shouldAwaitCRAs(roiDir: File): Result<Boolean> {
        return try {
            Result.success(!crasIngestedFile(roiDir).exists())
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun awaitCRAs(roiDir: File, cra: CRA): Result<Throwable?> {
        val cont = cra.contemporaryCRA
        val hist = cra.historicalCRA

        var contResult: ApiResult<Success>? = null
        var histResult: ApiResult<Success>? = null

        // returns when all children coroutines are complete
        coroutineScope {
            if (hist != null && cont != hist) {
                launch { histResult = awaitCRAIngestion(hist.tableUploadOperationName, hist.shapefileStorageKey) }
            }
            launch { contResult = awaitCRAIngestion(cont.tableUploadOperationName, cont.shapefileStorageKey) }
        }

        val err = apiResultCheck(contResult, histResult)
        if (err != null) {
            return Result.success(err)
        }

        if (!contResult!!.data!!.success || !histResult!!.data!!.success) {
            return Result.success(Throwable())
        }

        Success.toFile(crasIngestedFile(roiDir), contResult!!.data!!)

        return Result.success(null)
    }

    private fun crasIngestedFile(roiDir: File) = File(File(roiDir, crasDir), crasIngestedFile)
    private suspend fun awaitCRAIngestion(name: String, key: String) = api.awaitCRAUpload(UploadName(name, key))

    companion object {
        const val crasDir = "cras"
        const val crasUnzipDir = "unzip"
        const val crasFile = "cras.json"
        const val crasIngestedFile = "ingested.json"
    }
}