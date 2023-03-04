package org.blueventures.gemdroid.model.roi

import com.google.android.gms.maps.model.LatLng
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
    fun getRois(filesDir: File): Flow<Result<List<File>>> = flow {
        emit(dataSource.getRois(filesDir))
    }.flowOn(ioDispatcher)

    fun saveRoi(
        filesDir: File,
        name: String,
        contYearStart: Int,
        contYearEnd: Int,
        histYearStart: Int,
        histYearEnd: Int,
        monthStart: Int,
        monthEnd: Int,
        indices: List<String>,
        points: List<LatLng>
    ): Flow<Result<Unit>>  = flow {
        emit(dataSource.saveRoi(filesDir, name, contYearStart, contYearEnd, histYearStart, histYearEnd, monthStart, monthEnd, indices, points))
    }.flowOn(ioDispatcher)

    fun deleteRoi(dir: File): Flow<Result<Unit>> = flow {
        emit(dataSource.deleteRoi(dir))
    }.flowOn(ioDispatcher)
}