package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import java.io.File
import java.io.InputStream

class AnalysisRepository(
    private val dataSource: AnalysisDatasource = AnalysisDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getStage(roiDir: File): Flow<Stage> = flow {
        emit(dataSource.getStage(roiDir))
    }.flowOn(ioDispatcher)

    fun getROI(roiDir: File): Flow<Result<ROI>> = flow {
        emit(dataSource.getROI(roiDir))
    }.flowOn(ioDispatcher)

    fun saveBuffersFile(roiDir: File, buffers: Buffers): Flow<Result<Unit>> = flow {
        emit(dataSource.saveBuffersFile(roiDir, buffers))
    }.flowOn(ioDispatcher)

    fun loadBuffersFile(roiDir: File): Flow<Result<Buffers>> = flow {
        emit(dataSource.loadBuffersFile(roiDir))
    }.flowOn(ioDispatcher)

    fun getBuffers(roi: ROI): Flow<ApiResult<Buffers>> = flow {
        emit(dataSource.getBuffers(roi))
    }.flowOn(ioDispatcher)

    fun saveBuffer(roiDir: File, roi: ROI, buffer: Int): Flow<Result<Unit>> = flow {
        val roiSaved = dataSource.saveROI(roiDir, roi)
        if (roiSaved.isFailure) {
            emit(roiSaved)
        } else {
            emit(dataSource.saveBuffer(roiDir, buffer))
        }
    }.flowOn(ioDispatcher)

    fun getVisualizeURLs(roi: ROI): Flow<ApiResult<VisualizeURLs>> = flow {
        emit(dataSource.getVisualizeURLs(roi))
    }.flowOn(ioDispatcher)

    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs): Flow<Result<Unit>> = flow {
        emit(dataSource.saveVisualizeURLs(roiDir, urls))
    }.flowOn(ioDispatcher)

    fun loadVisualizeURLs(roiDir: File): Flow<Result<VisualizeURLs>> = flow {
        emit(dataSource.loadVisualizeURLs(roiDir))
    }.flowOn(ioDispatcher)

    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = flow {
        emit(dataSource.getRemoteCRAs(callback))
    }.flowOn(ioDispatcher)

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?) = flow {
        emit(dataSource.validateLocalCRA(roiDir, files, names, remoteCRAs, previous))
    }.flowOn(ioDispatcher)

    fun getCRAFields(cont: CRAFile, hist: CRAFile?, callback: (Result<Fields>) -> Unit) = flow {
        emit(dataSource.getCRAFields(cont, hist, callback))
    }.flowOn(ioDispatcher)

    fun uploadCRAs(c1: CRAFile, c2: CRAFile, callback: (Result<Unit>) -> Unit) = flow {
        emit(dataSource.uploadCRAs(c1, c2, callback))
    }.flowOn(ioDispatcher)

    fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) = flow {
        emit(dataSource.uploadCRA(cra, callback))
    }.flowOn(ioDispatcher)

    fun saveCRAs(roiDir: File, cra: CRA) = flow {
        emit(dataSource.saveCRAs(roiDir, cra))
    }.flowOn(ioDispatcher)

    fun chotTileDir(roiDir: File): File = dataSource.chotTileDir(roiDir)
    fun clotTileDir(roiDir: File): File = dataSource.clotTileDir(roiDir)
    fun hhotTileDir(roiDir: File): File = dataSource.hhotTileDir(roiDir)
    fun hlotTileDir(roiDir: File): File = dataSource.hlotTileDir(roiDir)
}