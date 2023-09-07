package org.blueventures.gemdroid.model.analysis

import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class AnalysisRepository(
    private val datasource: AnalysisDatasource = AnalysisDatasource(),
): ApiRepository(datasource) {
    fun getStage(roiDir: File) = goFlow { datasource.getStage(roiDir) }
    fun makeVisualizeTileDirs(roiDir: File) = goFlow { datasource.makeVisualizeTileDirs(roiDir) }
}