package org.blueventures.gemdroid.model.roi

import com.github.zibnix.droidbones.mvvm.FileService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class RoiRepository(
    private val dataSource: RoiDatasource = RoiDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): FileService() {
    fun getRois(filesDir: File): Flow<List<File>> = flow {
        emit(dataSource.getRois(filesDir))
    }.flowOn(ioDispatcher)
}