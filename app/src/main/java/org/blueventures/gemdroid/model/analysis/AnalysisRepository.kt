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
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class AnalysisRepository(
    private val datasource: AnalysisDatasource = AnalysisDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ApiRepository(datasource) {
    fun getStage(roiDir: File) = flow {
        emit(datasource.getStage(roiDir))
    }.flowOn(ioDispatcher)

    fun getROI(roiDir: File) = flow {
        emit(datasource.getROI(roiDir))
    }.flowOn(ioDispatcher)

    fun saveBuffersFile(roiDir: File, buffers: Buffers) = flow {
        emit(datasource.saveBuffersFile(roiDir, buffers))
    }.flowOn(ioDispatcher)

    fun loadBuffersFile(roiDir: File) = flow {
        emit(datasource.loadBuffersFile(roiDir))
    }.flowOn(ioDispatcher)

    fun getBuffers(roi: ROI) = flow {
        emit(datasource.getBuffers(roi))
    }.flowOn(ioDispatcher)

    fun saveBuffer(roiDir: File, roi: ROI, buffer: Int) = flow {
        val roiSaved = datasource.saveROI(roiDir, roi)
        if (roiSaved.isFailure) {
            emit(roiSaved)
        } else {
            emit(datasource.saveBuffer(roiDir, buffer))
        }
    }.flowOn(ioDispatcher)

    fun getVisualizeURLs(roi: ROI) = flow {
        emit(datasource.getVisualizeURLs(roi))
    }.flowOn(ioDispatcher)

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs) = flow {
        emit(datasource.saveVisualizeURLs(roiDir, urls))
    }.flowOn(ioDispatcher)

    fun loadVisualizeURLs(roiDir: File) = flow {
        emit(datasource.loadVisualizeURLs(roiDir))
    }.flowOn(ioDispatcher)

    fun chotTileDir(roiDir: File): File = datasource.chotTileDir(roiDir)
    fun clotTileDir(roiDir: File): File = datasource.clotTileDir(roiDir)
    fun hhotTileDir(roiDir: File): File = datasource.hhotTileDir(roiDir)
    fun hlotTileDir(roiDir: File): File = datasource.hlotTileDir(roiDir)
}