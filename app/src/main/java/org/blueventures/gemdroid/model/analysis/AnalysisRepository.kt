package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import java.io.File

class AnalysisRepository(
    private val dataSource: AnalysisDatasource = AnalysisDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getStage(roiDir: File): Flow<Stage> = flow {
        emit(dataSource.getStage(roiDir))
    }.flowOn(ioDispatcher)

    fun getROI(roiDir: File): Flow<ROI.Data?> = flow {
        emit(dataSource.getROI(roiDir))
    }.flowOn(ioDispatcher)

    fun saveBuffersFile(roiDir: File, buffers: Buffers.Data): Flow<Boolean> = flow {
        emit(dataSource.saveBuffersFile(roiDir, buffers))
    }.flowOn(ioDispatcher)

    fun getBuffersFile(roiDir: File): Flow<Buffers.Data?> = flow {
        emit(dataSource.getBuffersFile(roiDir))
    }.flowOn(ioDispatcher)

    fun getBuffers(roi: ROI.Data): Flow<ApiResult<Buffers.Data>> = flow {
        emit(dataSource.getBuffers(roi))
    }.flowOn(ioDispatcher)

    fun saveBuffer(roiDir: File, buffer: Int): Flow<Boolean> = flow {
        emit(dataSource.saveBuffer(roiDir, buffer))
    }.flowOn(ioDispatcher)
}