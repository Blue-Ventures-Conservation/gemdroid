package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.roi.RoiDatasource
import java.io.File

class AnalysisDatasource(
    api: Api.Service = Api.Service.instance(),
): ApiDatasource(api = api) {
    fun getStage(roiDir: File): Stage {
        return try {
            when {
                !File(roiDir, bufferFile).exists() -> Stage.BUFFER
                !File(File(roiDir, CRADatasource.crasDir), CRADatasource.crasFile).exists() -> Stage.CRAS
                !File(File(roiDir, ClassificationDatasource.classificationDir), ClassificationDatasource.classificationURLsFile).exists() -> Stage.CLASSIFICATION
                else -> Stage.ALL
            }
        } catch(e: Exception) {
            Stage.ERROR
        }
    }

    fun makeVisualizeTileDirs(roiDir: File): Result<Unit> {
        visualizeTileDirs.forEach { subdir ->
            val subdirRes = FileService.createDir(visDir(roiDir), subdir)
            if (subdirRes.isFailure) {
                return Result.failure(subdirRes.exceptionOrNull()!!)
            }
        }

        return Result.success(Unit)
    }

    companion object {
        // Buffer
        private const val bufferFile = "buffer_dist.json"
        private const val buffersChartFile = "buffers_chart.json"

        // Visualize
        private const val visualizeURLsFile = "urls.json"
        private const val visualizeDir = "visualize"
        private const val chotTilesDir = "chot_tiles"
        private const val clotTilesDir = "clot_tiles"
        private const val hhotTilesDir = "hhot_tiles"
        private const val hlotTilesDir = "hlot_tiles"
        private val visualizeTileDirs = arrayOf(chotTilesDir, clotTilesDir, hhotTilesDir, hlotTilesDir)

        // Downloads
        private const val exportsFilename = "landsat_exports.json"
        private const val resultsFile = "results.json"

        fun roiUUID() = RoiDatasource.roiUUID()
        fun roiFile(roiDir: File) = RoiDatasource.roiFile(roiDir)
        fun buffersFile(roiDir: File) = File(roiDir, buffersChartFile)
        fun buffDistFile(roiDir: File) = File(roiDir, bufferFile)
        fun urlsFile(roiDir: File) = File(visDir(roiDir), visualizeURLsFile)
        fun exportsFile(roiDir: File) = File(visDir(roiDir), exportsFilename)
        fun resultsFile(roiDir: File) = File(visDir(roiDir), resultsFile)
        fun visDir(roiDir: File) = File(roiDir, visualizeDir)
        fun chotTileDir(roiDir: File) = File(visDir(roiDir), chotTilesDir)
        fun clotTileDir(roiDir: File) = File(visDir(roiDir), clotTilesDir)
        fun hhotTileDir(roiDir: File) = File(visDir(roiDir), hhotTilesDir)
        fun hlotTileDir(roiDir: File) = File(visDir(roiDir), hlotTilesDir)
    }
}

enum class Stage {
    ERROR, BUFFER, CRAS, CLASSIFICATION, ALL
}