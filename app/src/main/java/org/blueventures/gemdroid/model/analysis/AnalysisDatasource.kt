package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Buffer
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.model.roi.RoiDatasource
import java.io.File

class AnalysisDatasource(
    private val backend: Api.BackendService = Api.BackendService.instance()
) {
    fun getStage(roiDir: File): Stage {
        return try {
            when {
                File(roiDir, dynamicsDir).exists() -> Stage.DONE
                File(roiDir, countryFile).exists() -> Stage.DYNAMICS
                File(roiDir, classificationDir).exists() -> Stage.COUNTRY
                File(roiDir, separabilityDir).exists() -> Stage.CLASSIFICATION
                File(roiDir, craFile).exists() -> Stage.SEPARABILITY
                File(roiDir, visualizeDir).exists() -> Stage.CRAS
                File(roiDir, bufferFile).exists() -> Stage.VISUALIZE
                else -> Stage.BUFFER
            }
        } catch(e: Exception) {
            Stage.ERROR
        }
    }

    fun getROI(roiDir: File) = ROI.fromFile(File(roiDir, roiFilename))
    fun saveROI(roiDir: File, roi: ROI) = ROI.toFile(File(roiDir, roiFilename), roi)
    fun saveBuffersFile(roiDir: File, buffers: Buffers) = Buffers.toFile(File(roiDir, buffersChartFile), buffers)
    fun loadBuffersFile(roiDir: File) = Buffers.fromFile(File(roiDir, buffersChartFile))
    suspend fun getBuffers(roi: ROI) = backend.getBuffers(roi)

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs): Boolean {
        visualizeTileDirs.forEach { subdir ->
            FileService.createDir(File(roiDir, visualizeDir), subdir) ?: return false
        }

        return VisualizeURLs.toFile(File(File(roiDir, visualizeDir), visualizeURLsFile), urls)
    }
    fun loadVisualizeURLs(roiDir: File) = VisualizeURLs.fromFile(File(File(roiDir, visualizeDir), visualizeURLsFile))
    fun deleteVisualizeURLs(roiDir: File) = FileService.deleteFile(File(File(roiDir, visualizeDir), visualizeURLsFile))
    fun chotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), chotTilesDir)
    fun clotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), clotTilesDir)
    fun hhotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), hhotTilesDir)
    fun hlotTileDir(roiDir: File): File = File(File(roiDir, visualizeDir), hlotTilesDir)
    suspend fun getVisualizeURLs(roi: ROI) = backend.getVisualizeURLs(roi)

    fun saveBuffer(roiDir: File, buffer: Int): Boolean {
        return Buffer.toFile(File(roiDir, bufferFile), Buffer(buffer))
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