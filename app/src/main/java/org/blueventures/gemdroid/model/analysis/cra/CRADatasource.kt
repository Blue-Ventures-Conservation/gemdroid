package org.blueventures.gemdroid.model.analysis.cra

import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.suspendCancellableCoroutine
import net.iryndin.jdbf.core.DbfFieldTypeEnum
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.GeojsonPolygonFeature
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.cra.BothFieldsCounted
import org.blueventures.gemdroid.data.analysis.cra.CRAKey
import org.blueventures.gemdroid.data.analysis.cra.ClassCount
import org.blueventures.gemdroid.data.analysis.cra.ClassCounts
import org.blueventures.gemdroid.data.analysis.cra.ContemporaryAndHistoricalRemoteCRAFileInfo
import org.blueventures.gemdroid.data.analysis.cra.Fields
import org.blueventures.gemdroid.data.analysis.cra.FieldsCounts
import org.blueventures.gemdroid.data.analysis.cra.LocalOrRemoteCRAFile
import org.blueventures.gemdroid.data.analysis.cra.RemoteCRAFileInfo
import org.blueventures.gemdroid.data.analysis.cra.StringsNumerics
import org.blueventures.gemdroid.data.analysis.cra.Success
import org.blueventures.gemdroid.data.analysis.cra.UploadName
import org.blueventures.gemdroid.data.polyfile.PolyFile
import org.blueventures.gemdroid.data.shp.Shapefile
import org.blueventures.gemdroid.model.SignIn
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.api.complete
import org.blueventures.gemdroid.model.api.storage
import org.blueventures.gemdroid.model.resultCheck
import org.blueventures.gemdroid.ui.analysis.cra.screens.Common
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.theme.Chartreuse
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

// TODO: go through this file and anywhere that accesses /shps for a user, make sure it knows how to handle that alternate json path as well
class CRADatasource(
    private val api: Api.Service = Api.Service.instance(),
    private val auth: FirebaseAuth = Firebase.auth,
    private val storage: FirebaseStorage = Firebase.storage,
): ApiDatasource(api, auth, storage) {
    suspend fun getRemoteCRANames(): Result<List<String>> {
        var shpsRes: Result<List<String>> = Result.failure(SignIn.not)
        var geojsonsRes: Result<List<String>> = Result.failure(SignIn.not)
        coroutineScope {
            shpsRes = getRemoteCRAList("shps")
            geojsonsRes = getRemoteCRAList("geojsons")
        }

        val err = resultCheck(shpsRes, geojsonsRes)
        if (err != null) return Result.failure(err)

        return Result.success(shpsRes.getOrNull()!!.toMutableList().apply {
            addAll(geojsonsRes.getOrNull()!!)
        })
    }

    private suspend fun getRemoteCRAList(filetypes: String): Result<List<String>> = suspendCancellableCoroutine { cont ->
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/$filetypes").listAll().storage(cont) { result ->
                val files = mutableListOf<String>()
                result.items.forEach {
                    if (it.name.endsWith(".json")) {
                        files.add(it.name.substringBeforeLast("."))
                    }
                }
                files
            }
        } ?: run {
            cont.resume(Result.failure(SignIn.not))
        }
    }

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean): Result<LocalOrRemoteCRAFile> {
        val crasDir = crasDir(roiDir)
        return validateShapes(crasDir, files, names, remoteCRAs, previous, overwrite)
    }

    private data class OrderedField<T>(val fieldValues: MutableList<T>, val fieldCounts: MutableList<Int>) {
        fun size() = fieldValues.size
        fun indexOf(value: T) = fieldValues.indexOf(value)
    }

    private fun validateShapes(crasDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean): Result<LocalOrRemoteCRAFile> {
        val strings = mutableListOf<String>()
        val stringsMap = mutableMapOf<String, OrderedField<String>>()
        val stringValues = mutableMapOf<String, List<String>>()

        val numerics = mutableListOf<String>()
        val numericsMap = mutableMapOf<String, OrderedField<Int>>()
        val numericValues = mutableMapOf<String, List<Int>>()

        val pathsResult = PolyFile.unzipOrCopy(crasDir, files, names)
        if (pathsResult.isFailure) return Result.failure(pathsResult.exceptionOrNull()!!)

        val zipResult = Shapefile.parse(crasDir, pathsResult.getOrNull()!!, nameCheck = { shpName ->
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
                            val numericVal = stringVal.toFloat().toInt()
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
                if (nof.size() == sof.size() && nof.fieldCounts == sof.fieldCounts) {
                    matched = true

                    val numbers = nof.fieldValues
                    val sorted = numbers.zip(sof.fieldValues).sortedBy { it.first }
                    val sofValues = sorted.map { it.second }

                    if (!strings.contains(sf)) {
                        strings.add(sf)
                        stringValues[sf] = sofValues
                    }
                }
            }

            if (matched) {
                numerics.add(nf)
                numericValues[nf] = nof.fieldValues.sorted()
            }
        }

        if (numerics.isEmpty()) {
            return Result.failure(NoStack(R.string.shp_no_candidate_num))
        }

        if (strings.isEmpty()) {
            return Result.failure(NoStack(R.string.shp_no_candidate_char))
        }

        val stringCounts = toClassCounts(stringsMap)
        val numericCounts = toClassCounts(numericsMap)

        return Result.success(
            LocalOrRemoteCRAFile(
                localFile = zipFile,
                counted = FieldsCounts(
                    Fields(numerics, strings, stringValues, numericValues),
                    StringsNumerics(ClassCounts(stringCounts), ClassCounts(numericCounts))
                )
            )
        )
    }

    private fun <T> addToMap(m: MutableMap<String, OrderedField<T>>, field: String, value: T){
        var of = m[field]
        if (of == null) {
            of = OrderedField(mutableListOf(), mutableListOf())
            m[field] = of
        }

        val i = of.indexOf(value)
        if (i < 0) {
            of.fieldValues.add(value)
            of.fieldCounts.add(1)
        } else {
            of.fieldCounts[i] += 1
        }
    }

    private fun <T> toClassCounts(m: MutableMap<String, OrderedField<T>>): Map<String, List<ClassCount>> {
        return m.entries.associateByTo(mutableMapOf(), { entry ->
            entry.key
        }) { entry ->
            val orderedField = entry.value
            val counts = mutableListOf<ClassCount>()
            for ((i, fieldValue) in orderedField.fieldValues.withIndex()) {
                counts.add(ClassCount(
                    fieldValue.toString(),
                    fieldValue as? Int ?: Int.MIN_VALUE,
                    craCount = entry.value.fieldCounts[i]
                ))
            }
            counts.toList()
        }
    }

    suspend fun getCRAFields(hist: LocalOrRemoteCRAFile?, cont: LocalOrRemoteCRAFile): Result<BothFieldsCounted> {
        if (hist == null || cont.equivalent(hist)) {
            val res = craFields(cont)
            if (res.isFailure) return Result.failure(res.exceptionOrNull()!!)
            val fieldsCounts = res.getOrNull()!!
            return Result.success(BothFieldsCounted(fieldsCounts.fields, fieldsCounts.counts, fieldsCounts.counts))
        }

        var histRes: Result<FieldsCounts> = Result.failure(Throwable())
        var contRes: Result<FieldsCounts> = Result.failure(Throwable())
        coroutineScope {
            launch { histRes = craFields(hist) }
            launch { contRes = craFields(cont) }
        }
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
                    val nvx = mutableMapOf<String, List<Int>>()

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

    private suspend fun craFields(cra: LocalOrRemoteCRAFile): Result<FieldsCounts> {
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
    private suspend fun fetchFields(cra: LocalOrRemoteCRAFile, key: String, uid: String): Result<FieldsCounts> = suspendCancellableCoroutine { cont ->
        val tmp = File.createTempFile("cras", "json")
        tmp.deleteOnExit()
        val filetypes = cra.filetypes()
        storage.reference.child("users/$uid/$filetypes/$key.json").getFile(tmp).storage(cont) {
            val infoRes = RemoteCRAFileInfo.fromFile(tmp)
            if (infoRes.isFailure) {
                cont.resume(Result.failure(infoRes.exceptionOrNull()!!))
                null
            } else {
                val info = infoRes.getOrNull()!!
                cra.eeUploadName = info.tableUploadOperationName
                FieldsCounts(
                    Fields(
                        chosenNumeric = info.numericClassField,
                        chosenString = info.stringClassField,
                        chosenStringValues = info.stringClassValues,
                    ),
                    StringsNumerics(ClassCounts(chosenCounts = info.classCounts ?: emptyList()))
                )
            }
        }
    }

    fun convertToCSVAndZip(roiDir: File, roiName: String, features: List<GeojsonPolygonFeature>): Result<File> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.getDefault()).format(Date())
        val filenameNoExt = "${roiName.lowercase().replace(" ", "_")}_${timestamp}"
        val csvFile = File(creationDir(roiDir), "$filenameNoExt.csv")
        val zipFile = File(creationDir(roiDir), "$filenameNoExt.zip")

        return try {
            val csvWriter = FileOutputStream(csvFile).bufferedWriter()
            // csv header
            csvWriter.write("geometry,$classIDPropertyKey,$classNumberPropertyKey,$classNamePropertyKey")
            csvWriter.newLine()

            val sortedFeatures = features.sortedBy { it.properties[classNumberPropertyKey] as Int }

            var id = 1
            for (feature in sortedFeatures) {
                val geoStr = GeojsonPolygon.adapter.toJson(feature.geometry)
                val classNumber = feature.properties[classNumberPropertyKey] as Int
                val className = feature.properties[classNamePropertyKey] as String
                csvWriter.write("\"$geoStr\",$id,$classNumber,$className")
                csvWriter.newLine()
                id += 1
            }
            csvWriter.flush()

            val zipResult = zipFile(listOf(csvFile), zipFile)
            if (zipResult.isFailure) {
                Result.failure(zipResult.exceptionOrNull()!!)
            } else {
                Result.success(zipFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteCreationJsonAndCSV(roiDir: File): Result<Unit> {
        val geojsonRes = FileService.deleteFile(creationGeojson(roiDir))
        for (file in FileService.getFiles(creationDir(roiDir), "csv")) {
            val res = FileService.deleteFile(file)
            if (res.isFailure) {
                return res
            }
        }
        return geojsonRes
    }

    suspend fun uploadCRAs(c1: LocalOrRemoteCRAFile, c2: LocalOrRemoteCRAFile): Result<Unit> {
        var r1: Result<Unit> = Result.failure(Throwable())
        var r2: Result<Unit> = Result.failure(Throwable())
        coroutineScope {
            launch { r1 = uploadCRA(c1) }
            launch { r2 = uploadCRA(c2) }
        }

        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)

        val ingestResult = ingestCRAs(c1, c2)
        if (ingestResult.isFailure) return ingestResult
        return uploadFields(c1, c2)
    }

    suspend fun uploadCRA(cra: LocalOrRemoteCRAFile): Result<Unit> {
        if (cra.localFile == null || !cra.counted.complete()) {
            return Result.failure(NoStack(R.string.internal_sho_err))
        }

        if (auth.currentUser?.uid == null) return Result.failure(SignIn.not)

        val uploadResult = uploadCRAFile(cra, auth.currentUser!!.uid, cra.localFile)
        if (uploadResult.isFailure) return uploadResult
        val ingestResult = ingestCRA(cra)
        if (ingestResult.isFailure) return ingestResult
        return uploadFields(cra)
    }

    private suspend fun uploadCRAFile(cra: LocalOrRemoteCRAFile, uid: String, file: File): Result<Unit> = suspendCancellableCoroutine { cont ->
        val filetypes = cra.filetypes()
        val key = cra.key()
        storage.reference.child("users/$uid/$filetypes/$key.zip").putFile(Uri.fromFile(file)).storage(cont)
    }

    suspend fun ingestCRAs(c1: LocalOrRemoteCRAFile, c2: LocalOrRemoteCRAFile): Result<Unit> {
        var r1: Result<Unit> = Result.failure(Throwable())
        var r2: Result<Unit> = Result.failure(Throwable())
        coroutineScope {
            launch { r1 = ingestCRA(c1) }
            launch { r2 = ingestCRA(c2) }
        }
        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)
        return Result.success(Unit)
    }

    suspend fun ingestCRA(cra: LocalOrRemoteCRAFile): Result<Unit> {
        val key = cra.key()
        val needed = ingestNeeded(cra.eeUploadName, key)
        if (needed.isFailure) return Result.failure(needed.exceptionOrNull()!!)
        if (!needed.getOrNull()!!) return Result.success(Unit)
        val result = api.ingestCRA(CRAKey(key, cra.isShapefile, cra.overwrite))
        val err = apiResultCheck(result)
        if (err != null) return Result.failure(err)
        val data = result.data!!
        if (!data.success) return Result.failure(NoStack(R.string.gee_ingestion_failed))
        cra.eeUploadName = data.name
        return Result.success(Unit)
    }

    private suspend fun ingestNeeded(name: String?, key: String): Result<Boolean> {
        if (name == null || name == "") return Result.success(true)
        val result = awaitCRAIngestion(name, key)
        val err = apiResultCheck(result)
        if (err != null) return Result.failure(err)
        return Result.success(result.data!!.ingestNeeded())
    }

    suspend fun uploadOneIngestOther(toUpload: LocalOrRemoteCRAFile, toIngest: LocalOrRemoteCRAFile): Result<Unit> {
        var uploadResult: Result<Unit> = Result.failure(Throwable())
        var ingestResult: Result<Unit> = Result.failure(Throwable())
        coroutineScope {
            launch { uploadResult = uploadCRA(toUpload) }
            launch { ingestResult = ingestCRA(toIngest) }
        }

        val err = resultCheck(uploadResult, ingestResult)
        if (err != null) return Result.failure(err)
        return Result.success(Unit)
    }

    private suspend fun uploadFields(c1: LocalOrRemoteCRAFile, c2: LocalOrRemoteCRAFile): Result<Unit> {
        var r1: Result<Unit> = Result.failure(Throwable())
        var r2: Result<Unit> = Result.failure(Throwable())

        coroutineScope {
            launch { r1 = uploadFields(c1) }
            launch { r2 = uploadFields(c2) }
        }

        val err = resultCheck(r1, r2)
        if (err != null) return Result.failure(err)
        return Result.success(Unit)
    }

    private suspend fun uploadFields(cra: LocalOrRemoteCRAFile): Result<Unit> {
        if (auth.currentUser?.uid == null) return Result.failure(SignIn.not)
        val uid = auth.currentUser!!.uid
        return try {
            uploadFields(cra, uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(IOException::class)
    private suspend fun uploadFields(cra: LocalOrRemoteCRAFile, uid: String): Result<Unit> = suspendCancellableCoroutine { cont ->
        val key = cra.key()
        val tmp = File.createTempFile(key, "json")
        tmp.deleteOnExit()
        val infoFileResult = RemoteCRAFileInfo.toFile(
            tmp,
            RemoteCRAFileInfo(
                cra.shpKey(),
                cra.jsonKey(),
                cra.eeUploadName!!,
                cra.counted.fields.chosenNumeric!!,
                cra.counted.fields.chosenString!!,
                cra.counted.fields.chosenStringValues!!,
                cra.counted.counts.stringCounts.chosenCounts
            )
        )

        if (infoFileResult.isFailure) {
            cont.resume(infoFileResult)
        } else {
            val filetypes = cra.filetypes()
            storage.reference.child("users/$uid/$filetypes/$key.json").putFile(Uri.fromFile(tmp)).storage(cont)
        }
    }

    // called after ingestion
    fun saveCRAs(roiDir: File, hist: LocalOrRemoteCRAFile?, cont: LocalOrRemoteCRAFile): Result<Unit> {
        val histCRA = if (hist == null) null else RemoteCRAFileInfo(
            hist.shpKey(),
            hist.jsonKey(),
            hist.eeUploadName!!,
            hist.counted.fields.chosenNumeric!!,
            hist.counted.fields.chosenString!!,
            hist.counted.fields.chosenStringValues!!,
            hist.counted.counts.stringCounts.chosenCounts
        )
        val contCRA = RemoteCRAFileInfo(
            cont.shpKey(),
            cont.jsonKey(),
            cont.eeUploadName!!,
            cont.counted.fields.chosenNumeric!!,
            cont.counted.fields.chosenString!!,
            cont.counted.fields.chosenStringValues!!,
            cont.counted.counts.stringCounts.chosenCounts
        )
        return ContemporaryAndHistoricalRemoteCRAFileInfo.toFile(crasFile(roiDir), ContemporaryAndHistoricalRemoteCRAFileInfo(histCRA, contCRA))
    }

    fun shouldAwaitCRAs(roiDir: File): Result<Boolean> {
        return try {
            Result.success(!crasIngestedFile(roiDir).exists())
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun awaitCRAs(roiDir: File, cras: ContemporaryAndHistoricalRemoteCRAFileInfo): Result<Unit> {
        val hist = cras.historicalCRA
        val cont = cras.contemporaryCRA

        var contResult: ApiResult<Success>? = null
        var histResult: ApiResult<Success>? = null

        // returns when all children coroutines are complete
        coroutineScope {
            if (hist != null && cont != hist) {
                launch { histResult = awaitCRAIngestion(hist.tableUploadOperationName, hist.storageKey()) }
            }
            launch { contResult = awaitCRAIngestion(cont.tableUploadOperationName, cont.storageKey()) }
        }

        val err = apiResultCheck(contResult, histResult)
        if (err != null) return Result.failure(err)

        if (contResult?.data?.success != true || (histResult != null && histResult.data?.success != true)) {
            return Result.failure(Throwable())
        }

        writeCRAsIngestedSuccess(roiDir)

        return Result.success(Unit)
    }

    private fun writeCRAsIngestedSuccess(roiDir: File) = Success.toFile(crasIngestedFile(roiDir), Success(true))

    private suspend fun awaitCRAIngestion(name: String, key: String) = api.awaitCRAIngestion(UploadName(name, key))

    private fun shouldIgnoreField(name: String): Boolean {
        for (ignore in ignoreFields) {
            if (name.startsWith(ignore, true)) {
                return true
            }
        }

        return false
    }

    object CRAPolygons {
        data class PolysWithClasses(val polys: MultiPolyPts, val classes: List<String>)
        data class ContemporaryAndHistoricalPolysWithClasses(val contemporary: PolysWithClasses, val historical: PolysWithClasses? = null)

        private fun loadLocalCRAShapefile(roiDir: File): Result<ContemporaryAndHistoricalPolysWithClasses> {
            val workDir = crasDir(roiDir)
            val zips = FileService.getFiles(workDir, "zip")

            if (zips.isEmpty()) {
                return Result.failure(Throwable())
            }

            val infoRes = loadRemoteCRAFilesInfo((roiDir))
            if (infoRes.isFailure) {
                return Result.failure(infoRes.exceptionOrNull()!!)
            }

            val contHist = infoRes.getOrNull()!!
            val contName = contHist.contemporaryCRA.shapefileStorageKey ?: "xunknownx"
            val histName = contHist.historicalCRA?.shapefileStorageKey ?: "xunknownx"
            var contZip: File? = null
            var histZip: File? = null
            for (zip in zips) {
                if (contZip == null && zip.name.contains(contName, false)) {
                    contZip = zip
                }
                if (histZip == null && zip.name.contains(histName, false)) {
                    histZip = zip
                }
            }

            if (contZip == null) {
                return Result.failure(Throwable())
            }

            val classNameProperty = contHist.contemporaryCRA.stringClassField
            val contRes = unzipAndGather(workDir, contZip, classNameProperty)
            if (contRes.isFailure) {
                return Result.failure(contRes.exceptionOrNull()!!)
            }

            val histPolys = if (histZip != null) {
                val res = unzipAndGather(workDir, histZip, classNameProperty)
                if (res.isSuccess) {
                    res.getOrNull()!!
                } else {
                    null
                }
            } else {
                null
            }

            return Result.success(ContemporaryAndHistoricalPolysWithClasses(contRes.getOrNull()!!, histPolys))
        }

        private fun unzipAndGather(workDir: File, zip: File, classNameProperty: String): Result<PolysWithClasses> {
            val unzipRes = FileService.unzip(FileInputStream(zip), workDir.path)
            if (unzipRes.isFailure) {
                return Result.failure(unzipRes.exceptionOrNull()!!)
            }

            val gatherRes = Shapefile.polygonsAndClasses(workDir, unzipRes.getOrNull()!!, classNameProperty)
            return when {
                gatherRes.isFailure -> Result.failure(gatherRes.exceptionOrNull()!!)
                else -> {
                    val pair = gatherRes.getOrNull()!!
                    Result.success(PolysWithClasses(pair.first, pair.second))
                }
            }
        }

        // currently only contemporary CRAs can be created locally as a CSV
        private fun loadLocalCraCsv(roiDir: File): Result<PolysWithClasses> {
            val workDir = creationDir(roiDir)
            val zips = FileService.getFiles(workDir, "zip")

            if (zips.isEmpty()) {
                return Result.failure(Throwable())
            }

            var contZip: File? = null
            for (zip in zips) {
                if (zip.name.contains(roiDir.name)) {
                    contZip = zip
                    break
                }
            }

            if (contZip == null) {
                return Result.failure(Throwable())
            }

            val unzipRes = FileService.unzip(FileInputStream(contZip), workDir.path)
            if (unzipRes.isFailure) {
                return Result.failure(unzipRes.exceptionOrNull()!!)
            }

            val files = unzipRes.getOrNull()!!
            if (files.isEmpty() || files.size != 1) {
                return Result.failure(Throwable())
            }

            try {
                val reader = FileInputStream(files[0]).bufferedReader()
                reader.readLine() // header
                val classNames = mutableListOf<String>()
                val polys = reader.lineSequence().mapNotNull { line ->
                    val (geometry, _, _, className) = line.split(',', ignoreCase = false, limit = 4)
                    val geojson = GeojsonPolygon.adapter.fromJson(geometry)
                    if (geojson == null) {
                        null
                    } else {
                        classNames.add(className)
                        GeojsonPolygon.toStateWithContainer(geojson).first
                    }
                }.toList()

                return Result.success(PolysWithClasses(polys, classNames))
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }

        private fun craPolysToUIPolys(menuTitle: String, polys: PolysWithClasses?): Polygons.Group? {
            if (polys == null || polys.polys.size != polys.classes.size) {
                return null
            }

            val named = mutableListOf<Polygons.Named>()
            for ((index, poly) in polys.polys.withIndex()) {
                val name = polys.classes[index]
                named.add(Polygons.Named(name, listOf(poly)))
            }

            return Polygons.Group(menuTitle, named, Chartreuse.toArgb())
        }

        private fun toUIPolys(contHistPolys: ContemporaryAndHistoricalPolysWithClasses, craPolygonsTitle: String, contemporaryPolygonsTitle: String, historicalPolygonsTitle: String): Result<List<Polygons.Group>> {
            val hist = craPolysToUIPolys(historicalPolygonsTitle, contHistPolys.historical)
            val cont = craPolysToUIPolys(if (hist == null) craPolygonsTitle else contemporaryPolygonsTitle, contHistPolys.contemporary)

            if (cont == null) {
                return Result.failure(Throwable())
            } else {
                val list = mutableListOf(cont)
                if (hist != null) {
                    list.add(hist)
                }
                return Result.success(list)
            }
        }

        fun load(roiDir: File, craPolygonsTitle: String, contemporaryPolygonsTitle: String, historicalPolygonsTitle: String): Result<List<Polygons.Group>> {
            val csvRes = loadLocalCraCsv(roiDir)
            if (csvRes.isSuccess) {
                return toUIPolys(ContemporaryAndHistoricalPolysWithClasses(csvRes.getOrNull()!!), craPolygonsTitle, contemporaryPolygonsTitle, historicalPolygonsTitle)
            }

            val shpRes = loadLocalCRAShapefile(roiDir)
            if (shpRes.isSuccess) {
                return toUIPolys(shpRes.getOrNull()!!, craPolygonsTitle, contemporaryPolygonsTitle, historicalPolygonsTitle)
            }

            return Result.failure(Throwable())
        }
    }

    companion object {
        fun loadRemoteCRAFilesInfo(roiDir: File) = ContemporaryAndHistoricalRemoteCRAFileInfo.fromFile(crasFile(roiDir))

        const val classIDPropertyKey = "ID"
        const val classNamePropertyKey = "classname"
        const val classNumberPropertyKey = "classnumber"
        const val collectionClassesPropertyKey = "classes"
        const val minCRAClasses = 2

        const val crasDir = "cras"
        const val creationDir = "cra_creation"
        const val creationGeojson = "created_cra.geojson"
        const val crasFile = "cras.json"
        const val crasIngestedFile = "ingested.json"

        private const val addedFieldShapeLen = "shape_len"
        private const val addedFieldShapeArea = "shape_area"
        private val ignoreFields = arrayOf(addedFieldShapeLen, addedFieldShapeArea)
        private fun crasDir(roiDir: File) = File(roiDir, crasDir)

        fun creationDir(roiDir: File) = File(crasDir(roiDir), creationDir)
        fun creationGeojson(roiDir: File) = File(creationDir(roiDir), creationGeojson)
        fun crasFile(roiDir: File) = File(crasDir(roiDir), crasFile)
        fun crasIngestedFile(roiDir: File) = File(crasDir(roiDir), crasIngestedFile)
    }
}