package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File

class ClassificationDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    suspend fun getClassification(classificationROI: ClassificationROI) = api.classification(classificationROI)
    fun saveClassificationFile(roiDir: File, urls: ClassificationURLs) = ClassificationURLs.toFile(File(classDir(roiDir), classificationURLsFile), urls)
    fun loadClassificationFile(roiDir: File) = ClassificationURLs.fromFile(File(classDir(roiDir), classificationURLsFile))
    fun contLCTileDir(roiDir: File) = File(classDir(roiDir), contLCTilesDir)
    fun histLCTileDir(roiDir: File) = File(classDir(roiDir), histLCTilesDir)

    private fun classDir(roiDir: File) = File(roiDir, classificationDir)

    companion object {
        const val classificationDir = "classification"
        const val contLCTilesDir = "cont_lc_tiles" // land cover tiles
        const val histLCTilesDir = "hist_lc_tiles" // land cover tiles
        const val classificationURLsFile = "urls.json"
    }
}