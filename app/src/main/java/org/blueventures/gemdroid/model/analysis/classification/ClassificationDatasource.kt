package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File

class ClassificationDatasource(
    api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    companion object {
        private const val classificationDir = "classification"
        private const val contLCTilesDir = "cont_lc_tiles" // land cover tiles
        private const val histLCTilesDir = "hist_lc_tiles" // land cover tiles
        private const val classificationURLsFile = "urls.json"
        private const val exportsFilename = "classification_exports.json"
        private const val resultsFile = "results.json"
        private fun classDir(roiDir: File) = File(roiDir, classificationDir)

        fun urlsFile(roiDir: File) = File(classDir(roiDir), classificationURLsFile)
        fun exportsFile(roiDir: File) = File(classDir(roiDir), exportsFilename)
        fun resultsFile(roiDir: File) = File(classDir(roiDir), resultsFile)
        fun contLCTileDir(roiDir: File) = File(classDir(roiDir), contLCTilesDir)
        fun histLCTileDir(roiDir: File) = File(classDir(roiDir), histLCTilesDir)
    }
}