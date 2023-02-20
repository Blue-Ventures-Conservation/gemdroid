package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import java.io.File

class AnalysisRepository(
    private val dataSource: AnalysisDatasource = AnalysisDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getStage(roiDir: File): Flow<Stage> = flow {
        emit(dataSource.getStage(roiDir))
    }.flowOn(ioDispatcher)

    fun getROI(roiDir: File): Flow<ROI?> = flow {
        emit(dataSource.getROI(roiDir))
    }.flowOn(ioDispatcher)

    fun saveBuffersFile(roiDir: File, buffers: Buffers): Flow<Boolean> = flow {
        emit(dataSource.saveBuffersFile(roiDir, buffers))
    }.flowOn(ioDispatcher)

    fun loadBuffersFile(roiDir: File): Flow<Buffers?> = flow {
        emit(dataSource.loadBuffersFile(roiDir))
    }.flowOn(ioDispatcher)

    fun getBuffers(roi: ROI): Flow<ApiResult<Buffers>> = flow {
        emit(dataSource.getBuffers(roi))
    }.flowOn(ioDispatcher)

    fun saveBuffer(roiDir: File, roi: ROI, buffer: Int): Flow<Boolean> = flow {
        val roiSaved = dataSource.saveROI(roiDir, roi)
        if (!roiSaved) {
            emit(false)
        } else {
            emit(dataSource.saveBuffer(roiDir, buffer))
        }
    }.flowOn(ioDispatcher)

    fun getVisualizeURLs(roi: ROI): Flow<ApiResult<VisualizeURLs>> = flow {
        emit(dataSource.getVisualizeURLs(roi))
    }.flowOn(ioDispatcher)

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs): Flow<Boolean> = flow {
        emit(dataSource.saveVisualizeURLs(roiDir, urls))
    }.flowOn(ioDispatcher)

    fun loadVisualizeURLs(roiDir: File): Flow<VisualizeURLs?> = flow {
        emit(dataSource.loadVisualizeURLs(roiDir))
    }.flowOn(ioDispatcher)

    fun chotTileDir(roiDir: File): File = dataSource.chotTileDir(roiDir)
    fun clotTileDir(roiDir: File): File = dataSource.clotTileDir(roiDir)
    fun hhotTileDir(roiDir: File): File = dataSource.hhotTileDir(roiDir)
    fun hlotTileDir(roiDir: File): File = dataSource.hlotTileDir(roiDir)
}