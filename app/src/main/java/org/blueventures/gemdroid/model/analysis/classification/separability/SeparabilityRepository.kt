package org.blueventures.gemdroid.model.analysis.classification.separability

import org.blueventures.gemdroid.data.analysis.cra.CraROI
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class SeparabilityRepository(
    private val datasource: SeparabilityDatasource = SeparabilityDatasource(),
): ApiRepository(datasource) {
    fun getSeparation(tp: TimePeriod, craROI: CraROI) = goFlow { datasource.getSeparation(tp, craROI) }
    fun saveSeparationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = goFlow { datasource.saveSeparationFile(roiDir, tp, data) }
    fun loadSeparationFile(roiDir: File, tp: TimePeriod) = goFlow { datasource.loadSeparationFile(roiDir, tp) }
    fun getScatter(tp: TimePeriod, craRoi: CraROI) = goFlow { datasource.getScatter(tp, craRoi) }
    fun saveScatterFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = goFlow { datasource.saveScatterFile(roiDir, tp, data) }
    fun loadScatterFile(roiDir: File, tp: TimePeriod) = goFlow { datasource.loadScatterFile(roiDir, tp) }
    fun getCorrelation(tp: TimePeriod, craRoi: CraROI) = goFlow { datasource.getCorrelation(tp, craRoi) }
    fun saveCorrelationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = goFlow { datasource.saveCorrelationFile(roiDir, tp, data) }
    fun loadCorrelationFile(roiDir: File, tp: TimePeriod) = goFlow { datasource.loadCorrelationFile(roiDir, tp) }
}