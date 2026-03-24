package org.blueventures.gemdroid.model.analysis.cra

import android.net.Uri
import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.apiResultCheck
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.iryndin.jdbf.core.DbfFieldTypeEnum
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.cra.CRAKey
import org.blueventures.gemdroid.data.analysis.cra.Success
import org.blueventures.gemdroid.data.analysis.cra.UploadName
import org.blueventures.gemdroid.data.polyfile.PolyFile
import org.blueventures.gemdroid.data.shp.ClassCount
import org.blueventures.gemdroid.data.shp.Shapefile
import org.blueventures.gemdroid.model.SignIn
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.resultCheck
import org.blueventures.gemdroid.ui.analysis.cra.screens.Common
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CRADatasource(
    private val api: Api.Service = Api.Service.instance(),
    private val auth: FirebaseAuth = Firebase.auth,
    private val storage: FirebaseStorage = Firebase.storage,
): ApiDatasource(api, auth, storage) {
    suspend fun getRemoteCRAs(): Result<List<String>> = suspendCoroutine { resumer ->
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/shps").listAll()
                .addOnSuccessListener { result ->
                    val files = mutableListOf<String>()
                    result.items.forEach {
                        if (it.name.endsWith(".json")) {
                            files.add(it.name.substringBeforeLast("."))
                        }
                    }
                    resumer.resume(Result.success(files))
                }
                .addOnFailureListener {
                    resumer.resume(Result.failure(NoStack(R.string.could_not_reach_storage)))
                }
        } ?: run {
            resumer.resume(Result.failure(SignIn.not))
        }
    }

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean): Result<CRAFile> {
        val crasDir = File(roiDir, crasDir)
        return validateShapes(crasDir, files, names, remoteCRAs, previous, overwrite)
    }

    private data class OrderedField(val values: MutableList<String>, val counts: MutableList<Int>) {
        fun size() = values.size
        fun indexOf(field: String) = values.indexOf(field)
    }

    private fun validateShapes(crasDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean): Result<CRAFile> {
        val strings = mutableListOf<String>()
        val stringsMap = mutableMapOf<String, OrderedField>()
        val stringValues = mutableMapOf<String, List<String>>()

        val numerics = mutableListOf<String>()
        val numericsMap = mutableMapOf<String, OrderedField>()
        val numericValues = mutableMapOf<String, List<String>>()

        val pathsResult = PolyFile.unzipOrCopy(crasDir, files, names)
        if (pathsResult.isFailure) return Result.failure(pathsResult.exceptionOrNull()!!)

        val zipResult = Shapefile.file(crasDir, pathsResult.getOrNull()!!, nameCheck = { shpName ->
            when {
                previous != null && previous == shpName -> NoStack(R.string.shps_must_differ)
                remoteCRAs.contains(shpName) && !overwrite -> Common.BadName(shpName)
                !Regexp.assetName.matches(shpName) -> NoStack(R.string.shp_name_alphanumeric)
                else -> null
            }
        }, recordf = { record ->
            for (field in record.fields) {
                val name = field.name

                val stringVal = record.getString(name)
                if (shouldIgnoreField(name) || stringVal == null) {
                    continue
                }

                when (field.type) {
                    DbfFieldTypeEnum.Numeric -> {
                        try {
                            val numericVal = stringVal.toFloat().toInt().toString()
                            addToMap(numericsMap, name, numericVal)
                        } catch (_: Exception) {}
                    }
                    DbfFieldTypeEnum.Character -> addToMap(stringsMap, name, stringVal)
                    else -> {}
                }
            }
            null
        })

        if (zipResult.isFailure) return Result.failure(zipResult.exceptionOrNull()!!)
        val zipFile = zipResult.getOrNull()!!

        numericsMap.forEach { (nf, nof) ->
            var matched = false
            stringsMap.forEach { (sf, sof) ->
                if (nof.size() == sof.size() && nof.counts == sof.counts) {
                    matched = true

                    val numbers = nof.values.map { it.toInt() }
                    val sorted = numbers.zip(sof.values).sortedBy { it.first }
                    val sofValues = sorted.map { it.second }

                    if (!strings.contains(sf)) {
                        strings.add(sf)
                        stringValues[sf] = sofValues
                    }
                }
            }

            if (matched) {
                numerics.add(nf)
                numericValues[nf] = nof.values.sortedBy { it.toInt() }
            }
        }

        if (numerics.size <= 0) {
            return Result.failure(NoStack(R.string.shp_no_candidate_num))
        }

        if (strings.size <= 0) {
            return Result.failure(NoStack(R.string.shp_no_candidate_char))
        }

        val stringCounts = toClassCounts(stringsMap)
        val numericCounts = toClassCounts(numericsMap)

        return Result.success(CRAFile(
            localFile = zipFile,
            counted = FieldsCounts(
                Fields(numerics, strings, stringValues, numericValues),
                StringsNumerics(ClassCounts(stringCounts), ClassCounts(numericCounts))
            ))
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

    private fun toClassCounts(m: MutableMap<String, OrderedField>): Map<String, List<ClassCount>> {
        return m.entries.associateByTo(mutableMapOf(), { entry ->
            entry.key
        }) { entry ->
            val counts = mutableListOf<ClassCount>()
            for ((i, fieldValue) in entry.value.values.withIndex()) {
                counts.add(ClassCount(
                    fieldValue,
                    -1,
                    craCount = entry.value.counts[i]
                ))
            }
            counts.toList()
        }
    }

    suspend fun getCRAFields(hist: CRAFile?, cont: CRAFile): Result<BothFieldsCounted> {
        if (hist == null || cont.equivalent(hist)) {
            val res = craFields(cont)
            if (res.isFailure) return Result.failure(res.exceptionOrNull()!!)
            val fieldsCounts = res.getOrNull()!!
            return Result.success(BothFieldsCounted(fieldsCounts.fields, fieldsCounts.counts, fieldsCounts.counts))
        }
        val histRes = craFields(hist)
        val contRes = craFields(cont)
        val err = resultCheck(histRes, contRes)
        if (err != null) return Result.failure(err)

        val histFieldsCounts = histRes.getOrNull()!!
        val contFieldsCounts = contRes.getOrNull()!!
        val mergeResult = mergeFields(histFieldsCounts.fields, contFieldsCounts.fields)
        if (mergeResult.isFailure) return Result.failure(mergeResult.exceptionOrNull()!!)
        val finalFields = mergeResult.getOrNull()!!

        return Result.success(BothFieldsCounted(finalFields, histFieldsCounts.counts, contFieldsCounts.counts))
    }

    private fun mergeFields(hist: Fields, cont: Fields): Result<Fields> {
        val mismatch = NoStack(R.string.shps_must_match_fields)
        return when {
            hist.complete() && cont.complete() -> {
                if (hist.chosenNumeric == cont.chosenNumeric && hist.chosenString == cont.chosenString && hist.chosenStringValues!!.containsAll(cont.chosenStringValues!!)) {
                    Result.success(hist)
                } else {
                    Result.failure(mismatch)
                }
            }
            hist.complete() && !cont.complete() -> {
                if (cont.numerics!!.contains(hist.chosenNumeric) && cont.strings!!.contains(hist.chosenString) && cont.stringValues!![hist.chosenString]!!.containsAll(hist.chosenStringValues!!)) {
                    Result.success(hist)
                } else {
                    Result.failure(mismatch)
                }
            }
            cont.complete() && !hist.complete() -> {
                if (hist.numerics!!.contains(cont.chosenNumeric) && hist.strings!!.contains(cont.chosenString) && hist.stringValues!![cont.chosenString]!!.containsAll(cont.chosenStringValues!!)) {
                    Result.success(cont)
                } else {
                    Result.failure(mismatch)
                }
            }
            !hist.complete() && !cont.complete() -> {
                val n1 = hist.numerics!!
                val n2 = cont.numerics!!
                val s1 = hist.strings!!
                val s2 = cont.strings!!
                val sv1 = hist.stringValues!!
                val sv2 = cont.stringValues!!
                val nv1 = hist.numericValues!!
                val nv2 = cont.numericValues!!

                val sx = s1.intersect(s2.toSet())
                val nx = n1.intersect(n2.toSet())
                if (sx.isNotEmpty() && nx.isNotEmpty()) {
                    var matches = 0
                    val svx = mutableMapOf<String, List<String>>()
                    val nvx = mutableMapOf<String, List<String>>()

                    for (s in sx) {
                        if (sv1[s]!! == sv2[s]!!) {
                            for (n in nx) {
                                if (nv1[n]!! == nv2[n]!! && sv1[s]!!.size == nv1[n]!!.size) {
                                    svx[s] = sv1[s]!!
                                    nvx[n] = nv1[n]!!
                                    matches++
                                    break
                                }
                            }
                        }
                    }

                    if (matches > 0) {
                        Result.success(Fields(nx.toList(), sx.toList(), svx, nvx))
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

    private suspend fun craFields(cra: CRAFile): Result<FieldsCounts> {
        if (cra.counted.parsedLocally() || cra.counted.complete()) return Result.success(cra.counted)
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
    private suspend fun fetchFields(cra: CRAFile, key: String, uid: String): Result<FieldsCounts> = suspendCoroutine { cont ->
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
                    Result.success(FieldsCounts(
                        Fields(
                            chosenNumeric = shp.numericClassField,
                            chosenString = shp.stringClassField,
                            chosenStringValues = shp.stringClassValues,
                        ),
                        StringsNumerics(ClassCounts(chosenCounts = shp.classCounts ?: emptyList()))
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
        if (cra.localFile == null || !cra.counted.complete()) {
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

    suspend fun ingestCRAs(roiDir: File, c1: CRAFile, c2: CRAFile): Result<Unit> {
        val r1 = ingestCRA(c1)
        val r2 = ingestCRA(c2)
        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)
        writeCRAsIngestedSuccess(roiDir)
        return Result.success(Unit)
    }

    suspend fun ingestCRA(cra: CRAFile): Result<Unit> {
        val key = cra.key()
        val needed = ingestNeeded(cra.eeUploadName, key)
        if (needed.isFailure) return Result.failure(needed.exceptionOrNull()!!)
        if (!needed.getOrNull()!!) return Result.success(Unit)
        val result = api.ingestCRA(CRAKey(key, cra.overwrite))
        val err = apiResultCheck(result)
        if (err != null) return Result.failure(err)
        val data = result.data!!
        if (!data.success) return Result.failure(NoStack(R.string.gee_ingestion_failed))
        cra.eeUploadName = data.name
        FileService.deleteFile(cra.localFile ?: File(""))
        return Result.success(Unit)
    }

    private suspend fun ingestNeeded(name: String?, key: String): Result<Boolean> {
        if (name == null || name == "") return Result.success(true)
        val result = awaitCRAIngestion(name, key)
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
        val shpRes = Shapefile.toFile(tmp,
            Shapefile(
                key,
                cra.eeUploadName!!,
                cra.counted.fields.chosenNumeric!!,
                cra.counted.fields.chosenString!!,
                cra.counted.fields.chosenStringValues!!,
                cra.counted.counts.stringCounts.chosenCounts
            )
        )

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

    suspend fun awaitCRAs(roiDir: File, cra: CRA): Result<Unit> {
        val hist = cra.historicalCRA
        val cont = cra.contemporaryCRA

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
            return Result.failure(err)
        }

        if (contResult?.data?.success != true || (histResult != null && histResult?.data?.success != true)) {
            return Result.failure(Throwable())
        }

        writeCRAsIngestedSuccess(roiDir)

        return Result.success(Unit)
    }

    private fun writeCRAsIngestedSuccess(roiDir: File) = Success.toFile(crasIngestedFile(roiDir), Success(true))

    private fun crasIngestedFile(roiDir: File) = File(File(roiDir, crasDir), crasIngestedFile)
    private suspend fun awaitCRAIngestion(name: String, key: String) = api.awaitCRAIngestion(UploadName(name, key))

    private fun shouldIgnoreField(name: String): Boolean {
        for (ignore in ignoreFields) {
            if (name.startsWith(ignore, true)) {
                return true
            }
        }

        return false
    }

    companion object {
        const val crasDir = "cras"
        const val crasFile = "cras.json"
        const val crasIngestedFile = "ingested.json"

        private const val addedFieldShapeLen = "shape_len"
        private const val addedFieldShapeArea = "shape_area"
        private val ignoreFields = arrayOf(addedFieldShapeLen, addedFieldShapeArea)
    }
}