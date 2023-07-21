package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Buffer
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource
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
                !File(roiDir, visualizeDir).exists() -> Stage.VISUALIZE
                !File(File(roiDir, CRADatasource.crasDir), CRADatasource.crasFile).exists() -> Stage.CRAS
                !File(roiDir, ClassificationDatasource.classificationDir).exists() -> Stage.CLASSIFICATION
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
    suspend fun getBuffers(roi: ROI) = api.getBuffers(roi)

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
    suspend fun getVisualizeURLs(roi: ROI) = api.getVisualizeURLs(roi)

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

        // CRA and Classification Stages in their own respective datasources

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
    ERROR, BUFFER, VISUALIZE, CRAS, CLASSIFICATION, DYNAMICS, DONE
}