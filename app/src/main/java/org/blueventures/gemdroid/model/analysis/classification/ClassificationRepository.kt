package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class ClassificationRepository(
    private val datasource: ClassificationDatasource = ClassificationDatasource(),
): ApiRepository(datasource) {
    fun getClassification(classificationROI: ClassificationROI) = goFlow { datasource.getClassification(classificationROI) }
    fun saveClassificationFile(roiDir: File, urls: ClassificationURLs) = goFlow { datasource.saveClassificationFile(roiDir, urls) }
    fun loadClassificationFile(roiDir: File) = goFlow { datasource.loadClassificationFile(roiDir) }
    fun contLCTileDir(roiDir: File) = datasource.contLCTileDir(roiDir)
    fun histLCTileDir(roiDir: File) = datasource.histLCTileDir(roiDir)
}