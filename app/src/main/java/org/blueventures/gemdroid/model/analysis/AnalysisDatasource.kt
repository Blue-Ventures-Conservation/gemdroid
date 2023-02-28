package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import net.iryndin.jdbf.reader.DbfReader
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Buffer
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.model.roi.RoiDatasource
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

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
    fun saveBuffer(roiDir: File, buffer: Int): Boolean {
        return Buffer.toFile(File(roiDir, bufferFile), Buffer(buffer))
    }
    suspend fun getBuffers(roi: ROI) = backend.getBuffers(roi)

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs): Boolean {
        visualizeTileDirs.forEach { subdir ->
            FileService.createDir(File(roiDir, visualizeDir), subdir) ?: return false
        }

        return VisualizeURLs.toFile(File(File(roiDir, visualizeDir), visualizeURLsFile), urls)
    }
    fun loadVisualizeURLs(roiDir: File) = VisualizeURLs.fromFile(File(File(roiDir, visualizeDir), visualizeURLsFile))
    fun chotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), chotTilesDir)
    fun clotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), clotTilesDir)
    fun hhotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), hhotTilesDir)
    fun hlotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), hlotTilesDir)
    suspend fun getVisualizeURLs(roi: ROI) = backend.getVisualizeURLs(roi)

    fun getRemoteCRAs(callback: (List<String>?, String) -> Unit) {
        auth.currentUser?.uid?.let { uid ->
            storage.reference.child("users/$uid/cras").listAll()
                .addOnSuccessListener { result ->
                    val files = arrayListOf<String>()
                    result.items.forEach {
                        if (it.name.endsWith(".zip")) {
                            files.add(it.name)
                        }
                    }
                    callback(files, "")
                }
                .addOnFailureListener {
                    callback(null, it.message ?: "Error communicating with Cloud Storage")
                }
        } ?: run {
            callback(null, "You don't appear to be logged in!")
        }
    }

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>): Result<File> {
        val crasDir = FileService.createDir(roiDir, crasDir) ?: return Result.failure(Throwable("Could not write to file system!"))

        return when {
            files.isEmpty() -> {
                Result.failure(Throwable("No file selected."))
            }
            files.size == 1 -> {
                validateZip(crasDir, files[0], names[0])
            }
            else -> {
                validateShapes(crasDir, files, names)
            }
        }
    }

    private fun validateZip(crasDir: File, zip: InputStream?, name: String?): Result<File> {
        val notZip = Throwable("If one file is selected, it must be a .zip")
        if (name?.substringAfterLast(".")?.lowercase() != "zip") {
            return Result.failure(notZip)
        }

        if (zip == null) {
            return Result.failure(Throwable("Could not open selected .zip for validation"))
        }

        val noRead = Throwable("Could not inspect zip file, make sure it has no internal directories")
        val unzipDir = FileService.createDir(crasDir, crasUnzipDir) ?: return Result.failure(noRead)

        val paths = FileService.unzip(zip, unzipDir.path) ?: return Result.failure(noRead)

        val streams = arrayListOf<InputStream>()
        val names = arrayListOf<String>()
        paths.forEach { path ->
            streams.add(FileInputStream(path))
            names.add(path.substringAfterLast(FileService.sep))
        }

        return validateShapes(crasDir, streams, names)
    }

    private fun validateShapes(crasDir: File, shps: List<InputStream?>, names: List<String?>): Result<File> {
        val badShape = Throwable("Your shapefile must include a .shp, .shx, .dbf and .prj")
        if (shps.size != 4) {
            return Result.failure(badShape)
        }

        var shp: InputStream? = null
        var shpName = "shp"
        var shx: InputStream? = null
        var shxName = "shx"
        var dbf: InputStream? = null
        var dbfName = "dbf"
        var prj: InputStream? = null
        var prjName = "prj"

        shps.forEachIndexed { i, stream ->
            if (stream == null) {
                return Result.failure(Throwable("Could not open selected files for validation"))
            }

            when(names[i]?.substringAfterLast(".")) {
                "shp" -> {
                    shp = BufferedInputStream(stream, 8192)
                    shpName = names[i]!!.substringBeforeLast(".")
                }
                "shx" -> {
                    shx = BufferedInputStream(stream, 8192)
                    shxName = names[i]!!.substringBeforeLast(".")
                }
                "dbf" -> {
                    dbf = BufferedInputStream(stream, 8192)
                    dbfName = names[i]!!.substringBeforeLast(".")
                }
                "prj" -> {
                    prj = BufferedInputStream(stream, 8192)
                    prjName = names[i]!!.substringBeforeLast(".")
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

        try {
            dbf?.mark(1024*1024)
            DbfReader(dbf) // this constructor will inspect the file header
            dbf?.reset()

            shp?.mark(1024*1024)
            ShapeFileReader(shp) // this constructor will inspect the file header
            shp?.reset()
        } catch (e: Exception) {
            return Result.failure(Throwable("Shapefile and dbf could not be parsed, and may be corrupted!"))
        }

        val zipFile = File(crasDir, "$shpName.zip")

        if (!FileService.zip(arrayOf(shp!!, shx!!, dbf!!, prj!!), names, zipFile.path)) {
            return Result.failure(Throwable("Could not (re)zip shapefile."))
        }

        FileService.deleteDir(File(crasDir, crasUnzipDir))

        return Result.success(zipFile)
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