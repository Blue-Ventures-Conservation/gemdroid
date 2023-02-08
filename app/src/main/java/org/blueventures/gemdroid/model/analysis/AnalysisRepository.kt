package org.blueventures.gemdroid.model.analysis

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class AnalysisRepository(
    private val dataSource: AnalysisDatasource = AnalysisDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getStage(roiDir: File): Flow<Stage> = flow {
        emit(dataSource.getStage(roiDir))
    }.flowOn(ioDispatcher)
}