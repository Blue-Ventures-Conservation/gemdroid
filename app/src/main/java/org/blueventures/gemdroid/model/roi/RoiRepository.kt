package org.blueventures.gemdroid.model.roi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class RoiRepository(
    private val dataSource: RoiDatasource = RoiDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getRois(filesDir: File): Flow<List<File>> = flow {
        emit(dataSource.getRois(filesDir))
    }.flowOn(ioDispatcher)

    fun saveRoi(filesDir: File, roi: RoiState): Flow<Boolean>  = flow {
        emit(dataSource.saveRoi(filesDir, roi))
    }.flowOn(ioDispatcher)

    fun deleteRoi(dir: File): Flow<Boolean> = flow {
        emit(dataSource.deleteRoi(dir))
    }.flowOn(ioDispatcher)
}