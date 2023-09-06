package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.analysis.Buffer
import org.blueventures.gemdroid.data.analysis.Buffers
import org.blueventures.gemdroid.data.analysis.ImageryExports
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.roi.RoiDatasource
import java.io.File

class AnalysisDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api = api) {
    fun getStage(roiDir: File): Stage {
        return try {
            when {
                !File(roiDir, bufferFile).exists() -> Stage.BUFFER
                !File(File(roiDir, CRADatasource.crasDir), CRADatasource.crasFile).exists() -> Stage.CRAS
                else -> Stage.ALL
            }
        } catch(e: Exception) {
            Stage.ERROR
        }
    }

    fun getROI(roiDir: File) = ROI.fromFile(roiFile(roiDir))
    fun saveROI(roiDir: File, roi: ROI) = ROI.toFile(roiFile(roiDir), roi)
    private fun roiFile(roiDir: File) = File(roiDir, roiFilename)
    fun saveBuffersFile(roiDir: File, buffers: Buffers) = Buffers.toFile(buffersFile(roiDir), buffers)
    fun loadBuffersFile(roiDir: File) = Buffers.fromFile(buffersFile(roiDir))
    suspend fun getBuffers(roi: ROI) = api.getBuffers(roi)
    private fun buffersFile(roiDir: File) = File(roiDir, buffersChartFile)
    fun saveBuffer(roiDir: File, buffer: Int) = Buffer.toFile(File(roiDir, bufferFile), Buffer(buffer))

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs): Result<Unit> {
        visualizeTileDirs.forEach { subdir ->
            val subdirRes = FileService.createDir(visDir(roiDir), subdir)
            if (subdirRes.isFailure) {
                return Result.failure(subdirRes.exceptionOrNull()!!)
            }
        }

        return VisualizeURLs.toFile(urlsFile(roiDir), urls)
    }
    fun loadVisualizeURLs(roiDir: File) = VisualizeURLs.fromFile(urlsFile(roiDir))
    suspend fun getVisualizeURLs(roi: ROI) = api.getVisualizeURLs(roi)
    private fun urlsFile(roiDir: File) = File(visDir(roiDir), visualizeURLsFile)

    fun saveExports(roiDir: File, exports: ImageryExports) = ImageryExports.toFile(exportsFile(roiDir), exports)
    fun loadExports(roiDir: File) = ImageryExports.fromFile(exportsFile(roiDir))
    suspend fun getExports(roi: ROI) = api.exportLandsat(roi)
    private fun exportsFile(roiDir: File) = File(visDir(roiDir), exportsFilename)

    fun loadResults(roiDir: File) = loadTasksResults(resultsFile(roiDir))
    fun saveResults(roiDir: File, results: TasksResults) = saveTasksResults(resultsFile(roiDir), results)
    fun deleteResults(roiDir: File) = FileService.deleteFile(resultsFile(roiDir))
    private fun resultsFile(roiDir: File) = File(visDir(roiDir), resultsFile)

    private fun visDir(roiDir: File) = File(roiDir, visualizeDir)

    fun chotTileDir(roiDir: File) = File(visDir(roiDir), chotTilesDir)
    fun clotTileDir(roiDir: File) = File(visDir(roiDir), clotTilesDir)
    fun hhotTileDir(roiDir: File) = File(visDir(roiDir), hhotTilesDir)
    fun hlotTileDir(roiDir: File) = File(visDir(roiDir), hlotTilesDir)

    companion object {
        // Buffer
        const val bufferFile = "buffer_dist.json"
        const val buffersChartFile = "buffers_chart.json"

        // Visualize
        const val visualizeURLsFile = "urls.json"
        const val visualizeDir = "visualize"
        const val chotTilesDir = "chot_tiles"
        const val clotTilesDir = "clot_tiles"
        const val hhotTilesDir = "hhot_tiles"
        const val hlotTilesDir = "hlot_tiles"
        val visualizeTileDirs = arrayOf(chotTilesDir, clotTilesDir, hhotTilesDir, hlotTilesDir)

        // Downloads
        const val exportsFilename = "landsat_exports.json"
        const val resultsFile = "results.json"

        // roi file details
        const val roiFilename = RoiDatasource.filename
    }
}

enum class Stage {
    ERROR, BUFFER, CRAS, ALL
}