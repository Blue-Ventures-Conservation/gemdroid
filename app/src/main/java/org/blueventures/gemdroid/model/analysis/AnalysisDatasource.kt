package org.blueventures.gemdroid.model.analysis

import android.net.Uri
import com.github.zibnix.droidbones.mvvm.FileService
import com.github.zibnix.droidbones.mvvm.FileService.sep
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import net.iryndin.jdbf.reader.DbfReader
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Buffer
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.Shapefile
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.model.SignIn
import org.blueventures.gemdroid.model.roi.RoiDatasource
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

class AnalysisDatasource(
    private val backend: Api.BackendService = Api.BackendService.instance(),
    private val storage: FirebaseStorage = Firebase.storage,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    fun getStage(roiDir: File): Stage {
        return try {
            when {
                !File(roiDir, bufferFile).exists() -> Stage.BUFFER
                !File(roiDir, visualizeDir).exists() -> Stage.VISUALIZE
                !File(File(roiDir, crasDir), craFile).exists() -> Stage.CRAS
                !File(roiDir, separabilityDir).exists() -> Stage.SEPARABILITY
                !File(roiDir, classificationDir).exists() -> Stage.CLASSIFICATION
                !File(roiDir, countryFile).exists() -> Stage.COUNTRY
                !File(roiDir, dynamicsDir).exists() -> Stage.DYNAMICS
                else -> Stage.DONE
            }
        } catch(e: Exception) {
            Stage.ERROR
        }
    }

    fun getROI(roiDir: File) = ROI.fromFile(File(roiDir, roiFilename))
    fun saveROI(roiDir: File, roi: ROI) = ROI.toFile(File(roiDir, roiFilename), roi)
    fun saveBuffersFile(roiDir: File, buffers: Buffers) = Buffers.toFile(File(roiDir, buffersChartFile), buffers)
    fun loadBuffersFile(roiDir: File) = Buffers.fromFile(File(roiDir, buffersChartFile))
    fun saveBuffer(roiDir: File, buffer: Int) = Buffer.toFile(File(roiDir, bufferFile), Buffer(buffer))
    suspend fun getBuffers(roi: ROI) = backend.getBuffers(roi)

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs): Result<Unit> {
        visualizeTileDirs.forEach { subdir ->
            val subdirRes = FileService.createDir(File(roiDir, visualizeDir), subdir)
            if (subdirRes.isFailure) {
                return Result.failure(subdirRes.exceptionOrNull()!!)
            }
        }

        return VisualizeURLs.toFile(File(File(roiDir, visualizeDir), visualizeURLsFile), urls)
    }
    fun loadVisualizeURLs(roiDir: File) = VisualizeURLs.fromFile(File(File(roiDir, visualizeDir), visualizeURLsFile))
    fun chotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), chotTilesDir)
    fun clotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), clotTilesDir)
    fun hhotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), hhotTilesDir)
    fun hlotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), hlotTilesDir)
    suspend fun getVisualizeURLs(roi: ROI) = backend.getVisualizeURLs(roi)

    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/shps").listAll()
                .addOnSuccessListener { result ->
                    val files = arrayListOf<String>()
                    result.items.forEach {
                        if (it.name.endsWith(".json")) {
                            files.add(it.name.substringBeforeLast("."))
                        }
                    }
                    callback(Result.success(files))
                }
                .addOnFailureListener {
                    callback(Result.failure(Throwable(it)))
                }
        } ?: run {
            callback(Result.failure(SignIn.not))
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

        return when {
            files.isEmpty() -> Result.failure(Throwable("No file selected."))
            files.size == 1 -> validateShapes(crasDir, remoteCRAs, previous, unzip(unzipDir, files[0], names[0]))
            else -> validateShapes(crasDir, remoteCRAs, previous, copyShapes(unzipDir, files, names))
        }
    }

    private fun copyShapes(dir: File, shps: List<InputStream?>, names: List<String?>): Result<List<String>> {
        val paths = arrayListOf<String>()
        shps.forEachIndexed { i, shp ->
            if (shp == null || names[i] == null) {
                return Result.failure(Throwable("Could not read shapefiles"))
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
        val notZip = Throwable("If one file is selected, it must be a .zip")
        if (name?.substringAfterLast(".")?.lowercase() != "zip") {
            return Result.failure(notZip)
        }

        if (zip == null) {
            return Result.failure(Throwable("Could not open selected .zip for validation"))
        }

        val pathsRes = FileService.unzip(zip, dir.path)
        if (pathsRes.isFailure) {
            return Result.failure(pathsRes.exceptionOrNull()!!)
        }
        return Result.success(pathsRes.getOrNull()!!)
    }

    private fun validateShapes(crasDir: File, remoteCRAs: List<String>, previous: String?, pathsResult: Result<List<String>>): Result<CRAFile> {
        if (pathsResult.isFailure) {
            return Result.failure(pathsResult.exceptionOrNull()!!)
        }

        val paths = pathsResult.getOrNull()!!

        val badShape = Throwable("Your shapefile must include a .shp, .shx, .dbf and .prj")
        if (paths.size != 4) {
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
                    shpName = path.substringAfterLast(sep).substringBeforeLast(".")
                }
                "shx" -> {
                    shx = path
                    shxName = path.substringAfterLast(sep).substringBeforeLast(".")
                }
                "dbf" -> {
                    dbf = path
                    dbfName = path.substringAfterLast(sep).substringBeforeLast(".")
                }
                "prj" -> {
                    prj = path
                    prjName = path.substringAfterLast(sep).substringBeforeLast(".")
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
            return Result.failure(Throwable("Shapefiles should all have the same name."))
        }

        if (previous != null && previous == shpName) {
            return Result.failure(Throwable("Please select two different shapefiles."))
        }

        if (remoteCRAs.contains(shpName)) {
            return Result.failure(Throwable("Please use the previously uploaded shapefile by that name."))
        }

        val fields = arrayListOf<String>()
        try {
            // these constructors will inspect the file header
            val shpStream = FileInputStream(shp)
            ShapeFileReader(shpStream)
            shpStream.close()

            val r = DbfReader(FileInputStream(dbf))
            r.metadata.fields.forEach { field ->
                fields.add(field.name)
            }
            r.close()
        } catch (e: Exception) {
            return Result.failure(Throwable("Shapefile and dbf could not be parsed, and may be corrupted!"))
        }

        val zipFile = File(crasDir, "$shpName.zip")

        val zipRes = FileService.zip(arrayOf(shp!!, shx!!, dbf!!, prj!!), zipFile.path)
        if (zipRes.isFailure) {
            return Result.failure(zipRes.exceptionOrNull()!!)
        }

        FileService.deleteDir(File(crasDir, crasUnzipDir))

        return Result.success(CRAFile(
            localFile = zipFile,
            fields = Fields(fields)
        ))
    }

    fun getCRAFields(cont: CRAFile, hist: CRAFile?, callback: (Result<Fields>) -> Unit) {
        if (hist == null || cont.equivalent(hist)) {
            craFields(cont, callback)
            return
        }

        val successes = AtomicInteger()
        val failures = AtomicInteger()
        var contFields: Fields? = null
        var histFields: Fields? = null

        val handleErr: (Result<Fields>) -> Boolean = { result ->
            if (result.isFailure && failures.addAndGet(1) == 1) {
                callback(result)
                true
            } else {
                false
            }
        }

        craFields(cont) { result ->
            if (!handleErr(result)) {
                contFields = result.getOrNull()
                if (successes.addAndGet(1) == 2) {
                    mergeNullFields(contFields, histFields, callback)
                }
            }
        }

        craFields(hist) { result ->
            if (!handleErr(result)) {
                histFields = result.getOrNull()
                if (successes.addAndGet(1) == 2) {
                    mergeNullFields(histFields, contFields, callback)
                }
            }
        }
    }

    private fun mergeNullFields(f1: Fields?, f2: Fields?, callback: (Result<Fields>) -> Unit) {
        if (f1 == null || f2 == null) {
            return
        }

        callback(mergeFields(f1, f2))
    }

    private fun mergeFields(f1: Fields, f2: Fields): Result<Fields> {
        val mismatch = Throwable("Please select shapefiles that have matching fields")
        return when {
            f1.complete() && f2.complete() -> {
                if (f1.numeric == f2.numeric && f1.string == f2.string) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f1.complete() && !f2.complete() -> {
                if (f2.list!!.containsAll(listOf(f1.numeric!!, f1.string!!))) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            f2.complete() && !f1.complete() -> {
                if (f1.list!!.containsAll(listOf(f2.numeric!!, f2.string!!))) {
                    Result.success(f2)
                } else {
                    Result.failure(mismatch)
                }
            }
            !f1.complete() && !f2.complete() -> {
                val l1 = f1.list!!
                val l2 = f2.list!!
                if (l1.size == l2.size && l1.containsAll(l2)) {
                    Result.success(f1)
                } else {
                    Result.failure(mismatch)
                }
            }
            else -> {
                // should not be reachable
                Result.failure(Throwable("Unreachable error encountered..."))
            }
        }
    }

    private fun craFields(cra: CRAFile, callback: (Result<Fields>) -> Unit) {
        if (cra.fields.list != null) {
            callback(Result.success(cra.fields))
        } else {
            cra.storageKey?.let { key ->
                auth.currentUser?.uid?.let { uid ->
                    try {
                        val tmp = File.createTempFile("cras", "json")
                        storage.reference.child("users/$uid/shps/$key.json").getFile(tmp).addOnSuccessListener {
                            val shpRes = Shapefile.fromFile(tmp)
                            if (shpRes.isFailure) {
                                callback(Result.failure(shpRes.exceptionOrNull()!!))
                            } else {
                                val shp = shpRes.getOrNull()!!
                                callback(Result.success(Fields(
                                    numeric = shp.numericClassField,
                                    string = shp.stringClassField
                                )))
                            }
                        }.addOnFailureListener {
                            callback(Result.failure(Throwable(it)))
                        }
                    } catch (e: Exception) {
                        callback(Result.failure(Throwable(e)))
                    }
                } ?: run {
                    callback(Result.failure(SignIn.not))
                }
            } ?: run {
                callback(Result.failure(Throwable("Internal storage key error, sorry!")))
            }
        }
    }

    fun uploadCRAs(c1: CRAFile, c2: CRAFile, callback: (Result<Unit>) -> Unit) {
        val successes = AtomicInteger()
        val failures = AtomicInteger()

        val handleErr: (Result<Unit>) -> Boolean = { result ->
            if (result.isFailure && failures.addAndGet(1) == 1) {
                callback(result)
                true
            } else {
                false
            }
        }

        uploadCRA(c1) { result ->
            if (!handleErr(result)) {
                if (successes.addAndGet(1) == 2) {
                    callback(Result.success(Unit))
                }
            }
        }

        uploadCRA(c2) { result ->
            if (!handleErr(result)) {
                if (successes.addAndGet(1) == 2) {
                    callback(Result.success(Unit))
                }
            }
        }
    }

    fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) {
        if (cra.localFile == null || cra.fields.numeric == null || cra.fields.string == null) {
            callback(Result.failure(Throwable("Internal shapefile error, sorry!")))
        }

        val key = cra.key()
        val zip = cra.localFile!!
        uploadShapefile(key, zip) { result ->
            when {
                result.isSuccess -> uploadFields(key, cra.fields.numeric!!, cra.fields.string!!, callback)
                result.isFailure -> callback(result)
            }
        }
    }

    private fun uploadShapefile(key: String, zip: File, callback: (Result<Unit>) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/shps/$key.zip").putFile(Uri.fromFile(zip))
                .addOnSuccessListener {
                    callback(Result.success(Unit))
                }.addOnFailureListener {
                    callback(Result.failure(Throwable(it)))
                }
        } ?: run {
            callback(Result.failure(SignIn.not))
        }
    }

    private fun uploadFields(key: String, numeric: String, string: String, callback: (Result<Unit>) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            try {
                val tmp = File.createTempFile(key, "json")
                val shpRes = Shapefile.toFile(tmp, Shapefile(key, numeric, string))
                if (shpRes.isFailure) {
                    callback(shpRes)
                } else {
                    storage.reference.child("users/$uid/shps/$key.json").putFile(Uri.fromFile(tmp))
                        .addOnSuccessListener {
                            callback(Result.success(Unit))
                        }.addOnFailureListener {
                            callback(Result.failure(Throwable(it)))
                        }
                }
            } catch (e: Exception) {
                callback(Result.failure(Throwable(e)))
            }
        } ?: run {
            callback(Result.failure(SignIn.not))
        }
    }

    fun saveCRAs(roiDir: File, cra: CRA): Result<Unit> {
        val crasDirRes = FileService.createDir(roiDir, crasDir)
        if (crasDirRes.isFailure) {
            return Result.failure(crasDirRes.exceptionOrNull()!!)
        }
        return CRA.toFile(File(crasDirRes.getOrNull()!!, craFile), cra)
    }

    companion object {
        // Buffer Stage
        const val bufferFile = "buffer_dist.json"
        const val buffersChartFile = "buffers_chart.json"

        // Visualize Stage
        const val visualizeURLsFile = "urls.json"
        const val visualizeDir = "visualize"
        const val chotTilesDir = "chot_tiles"
        const val clotTilesDir = "clot_tiles"
        const val hhotTilesDir = "hhot_tiles"
        const val hlotTilesDir = "hlot_tiles"
        val visualizeTileDirs = arrayOf(chotTilesDir, clotTilesDir, hhotTilesDir, hlotTilesDir)

        // CRAs Stage
        const val crasDir = "cras"
        const val crasUnzipDir = "unzip"
        const val craUploading = "cras_uploading"
        const val craFile = "cras.json"

        // Separability Stage
        const val separabilityDir = "separability"
        const val chotSamplesFile = "chot_samples.json"
        const val clotSamplesFile = "clot_samples.json"
        const val hhotSamplesFile = "hhot_samples.json"
        const val hlotSamplesFile = "hlot_samples.json"
        const val chotCorrelationFile = "chot_corr.json"
        const val clotCorrelationFile = "clot_corr.json"
        const val hhotCorrelationFile = "hhot_corr.json"
        const val hlotCorrelationFile = "hlot_corr.json"
        const val chotLSBandsSeparationFile = "chot_ls_separation.json"
        const val clotLSBandsSeparationFile = "clot_ls_separation.json"
        const val hhotLSBandsSeparationFile = "hhot_ls_separation.json"
        const val hlotLSBandsSeparationFile = "hlot_ls_separation.json"
        const val chotIndicesSeparationFile = "chot_indices_separation.json"
        const val clotIndicesSeparationFile = "clot_indices_separation.json"
        const val hhotIndicesSeparationFile = "hhot_indices_separation.json"
        const val hlotIndicesSeparationFile = "hlot_indices_separation.json"

        // Classification Stage
        const val classificationDir = "classification"
        const val contLCTilesDir = "cont_lc_tiles"
        const val histLCTilesDir = "hist_lc_tiles"
        const val ccomClassifyFile = "ccom_classify.json"
        const val hcomClassifyFile = "hcom_classify.json"

        // Country Stage
        const val countryFile = "country"

        // Dynamics Stage
        const val dynamicsDir = "dynamics"
        const val dynamicsDataFile = "dynamics.json"
        const val gainTilesDir = "gain_tiles"
        const val lossTilesDir = "loss_tiles"
        const val persistenceTilesDir = "persistence_tiles"

        // roi file details
        const val roiFilename = RoiDatasource.filename
    }
}

enum class Stage {
    ERROR, BUFFER, VISUALIZE, CRAS, SEPARABILITY, CLASSIFICATION, COUNTRY, DYNAMICS, DONE
}