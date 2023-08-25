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
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.Shapefile.inspectAndZip
import org.blueventures.gemdroid.data.Shapefile.unzipOrCopy
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.cra.CRAKey
import org.blueventures.gemdroid.data.analysis.cra.Shapefile
import org.blueventures.gemdroid.data.analysis.cra.Success
import org.blueventures.gemdroid.data.analysis.cra.UploadName
import org.blueventures.gemdroid.model.SignIn
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.resultCheck
import java.io.File
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
        val crasDir = File(roiDir, crasDir)
        val unzipDir = File(crasDir, crasUnzipDir)
        val result = validateShapes(crasDir, remoteCRAs, previous, unzipOrCopy(unzipDir, files, names))
        FileService.deleteDir(unzipDir)
        return result
    }

    private data class OrderedField(val values: MutableList<String>, val counts: MutableList<Int>) {
        fun size() = values.size
        fun indexOf(field: String) = values.indexOf(field)
    }

    private fun validateShapes(crasDir: File, remoteCRAs: List<String>, previous: String?, pathsResult: Result<List<String>>): Result<CRAFile> {
        val numerics = mutableListOf<String>()
        val strings = mutableListOf<String>()
        val numericsMap = mutableMapOf<String, OrderedField>()
        val stringsMap = mutableMapOf<String, OrderedField>()
        val stringValues = mutableMapOf<String, List<String>>()

        val zipResult = inspectAndZip(crasDir, pathsResult, { shpName ->
            when {
                previous != null && previous == shpName -> NoStack(R.string.shps_must_differ)
                remoteCRAs.contains(shpName) -> NoStack(R.string.please_reuse_shp)
                !Regexp.assetName.matches(shpName) -> NoStack(R.string.shp_name_alphanumeric)
                else -> null
            }
        }) { _, record ->
            record.fields.forEach { field ->
                val name = field.name
                when (field.type) {
                    DbfFieldTypeEnum.Numeric -> addToMap(numericsMap, name, record.getString(name).toFloat().toInt().toString())
                    DbfFieldTypeEnum.Character -> addToMap(stringsMap, name, record.getString(name))
                    else -> {}
                }
            }
        }

        if (zipResult.isFailure) return Result.failure(zipResult.exceptionOrNull()!!)
        val zipFile = zipResult.getOrNull()!!

        numericsMap.forEach { (nf, nof) ->
            stringsMap.forEach { (sf, sof) ->
                if (nof.size() == sof.size()) {
                    numerics.add(nf)
                    if (!strings.contains(sf)) {
                        strings.add(sf)
                        stringValues[sf] = sof.values
                    }
                }
            }
        }

        if (numerics.size <= 0) {
            return Result.failure(NoStack(R.string.shp_no_candidate_num))
        }

        if (strings.size <= 0) {
            return Result.failure(NoStack(R.string.shp_no_candidate_char))
        }

        return Result.success(CRAFile(
            localFile = zipFile,
            fields = Fields(numerics, strings, stringValues))
        )
    }

    private fun addToMap(m: MutableMap<String, OrderedField>, field: String, value: String) {
        var of = m[field]
        if (of == null) {
            of = OrderedField(mutableListOf(), mutableListOf())
            m[field] = of
        }

        val i = of.indexOf(value)
        if (i < 0) {
            of.values.add(value)
            of.counts.add(1)
        } else {
            of.counts[i] = of.counts[i] + 1
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
                if (f1.chosenNumeric == f2.chosenNumeric && f1.chosenString == f2.chosenString && f1.chosenStringValues!!.containsAll(f2.chosenStringValues!!)) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f1.complete() && !f2.complete() -> {
                if (f2.numerics!!.contains(f1.chosenNumeric) && f2.strings!!.contains(f1.chosenString) && f2.stringValues!![f1.chosenString]!!.containsAll(f1.chosenStringValues!!)) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f2.complete() && !f1.complete() -> {
                if (f1.numerics!!.contains(f2.chosenNumeric) && f1.strings!!.contains(f2.chosenString) && f1.stringValues!![f2.chosenString]!!.containsAll(f2.chosenStringValues!!)) {
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
                if (s1.containsAll(s2) && n1.containsAll(n2)) {
                    var success = true

                    for (entry in sv1) {
                        if (sv2.contains(entry.key) && entry.value.containsAll(sv2[entry.key]!!)) {
                            continue
                        }

                        success = false
                        break
                    }

                    if (success) {
                        Result.success(f1)
                    } else {
                        Result.failure(mismatch)
                    }
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
        if (cra.localFile == null || !cra.fields.complete()) {
            return Result.failure(NoStack(R.string.internal_sho_err))
        }

        if (auth.currentUser?.uid == null) return Result.failure(SignIn.not)

        return uploadShapefile(cra.key(), auth.currentUser!!.uid, cra.localFile)
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
        FileService.deleteFile(cra.localFile ?: File(""))
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