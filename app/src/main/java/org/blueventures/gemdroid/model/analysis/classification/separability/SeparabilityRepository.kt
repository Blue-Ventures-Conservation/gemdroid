package org.blueventures.gemdroid.model.analysis.classification.separability

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.blueventures.gemdroid.data.analysis.cra.CraROI
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class SeparabilityRepository(
    private val datasource: SeparabilityDatasource = SeparabilityDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ApiRepository(datasource) {
    fun getSeparation(tp: TimePeriod, craROI: CraROI) = flow {
        emit(datasource.getSeparation(tp, craROI))
    }.flowOn(ioDispatcher)

    fun saveSeparationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = flow {
        emit(datasource.saveSeparationFile(roiDir, tp, data))
    }.flowOn(ioDispatcher)

    fun loadSeparationFile(roiDir: File, tp: TimePeriod) = flow {
        emit(datasource.loadSeparationFile(roiDir, tp))
    }.flowOn(ioDispatcher)

    fun getScatter(tp: TimePeriod, craRoi: CraROI) = flow {
        emit(datasource.getScatter(tp, craRoi))
    }.flowOn(ioDispatcher)

    fun saveScatterFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = flow {
        emit(datasource.saveScatterFile(roiDir, tp, data))
    }.flowOn(ioDispatcher)

    fun loadScatterFile(roiDir: File, tp: TimePeriod) = flow {
        emit(datasource.loadScatterFile(roiDir, tp))
    }.flowOn(ioDispatcher)

    fun getCorrelation(tp: TimePeriod, craRoi: CraROI) = flow {
        emit(datasource.getCorrelation(tp, craRoi))
    }.flowOn(ioDispatcher)

    fun saveCorrelationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = flow {
        emit(datasource.saveCorrelationFile(roiDir, tp, data))
    }.flowOn(ioDispatcher)

    fun loadCorrelationFile(roiDir: File, tp: TimePeriod) = flow {
        emit(datasource.loadCorrelationFile(roiDir, tp))
    }.flowOn(ioDispatcher)
}