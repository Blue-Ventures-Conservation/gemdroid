package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import java.io.File

class AnalysisDatasource(private val files: FileService = FileService()) {
    fun getStage(roiDir: File): Stage {
        return try {
            when {
                File(roiDir, dynamicsDir).exists() -> Stage.DONE
                File(roiDir, countryFile).exists() -> Stage.DYNAMICS
                File(roiDir, classificationDir).exists() -> Stage.COUNTRY
                File(roiDir, separabilityDir).exists() -> Stage.CLASSIFICATION
                File(roiDir, craFile).exists() -> Stage.SEPARABILITY
                File(roiDir, colorsFiles).exists() -> Stage.CRAS
                File(roiDir, visualizeDir).exists() -> Stage.COLORS
                File(roiDir, bufferFile).exists() -> Stage.VISUALIZE
                else -> Stage.BUFFER
            }
        } catch(e: Exception) {
            Stage.ERROR
        }
    }

    companion object {
        // Buffer Stage
        const val bufferFile = "buffer_dist.json"

        // Visualize Stage
        const val visualizeDir = "visualize"
        const val chotTilesDir = "chot_tiles"
        const val clotTilesDir = "clot_tiles"
        const val hhotTilesDir = "hhot_tiles"
        const val hlotTilesDir = "hlot_tiles"

        // Colors Stage
        const val colorsFiles = "colors.json"

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
    }
}

enum class Stage {
    ERROR, BUFFER, VISUALIZE, COLORS, CRAS, SEPARABILITY, CLASSIFICATION, COUNTRY, DYNAMICS, DONE
}