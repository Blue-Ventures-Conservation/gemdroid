package org.blueventures.gemdroid.model.roi

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class RoiRepository(
    private val datasource: RoiDatasource = RoiDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getRois(filesDir: File) = flow {
        emit(datasource.getRois(filesDir))
    }.flowOn(ioDispatcher)

    fun saveRoi(
        filesDir: File,
        name: String,
        contYearStart: Int,
        contYearEnd: Int,
        contMonthStart: Int,
        contMonthEnd: Int,
        histYearStart: Int,
        histYearEnd: Int,
        histMonthStart: Int,
        histMonthEnd: Int,
        indices: List<String>,
        points: List<LatLng>
    ): Flow<Result<Unit>>  = flow {
        emit(datasource.saveRoi(filesDir, name, contYearStart, contYearEnd, contMonthStart, contMonthEnd, histYearStart, histYearEnd, histMonthStart, histMonthEnd, indices, points))
    }.flowOn(ioDispatcher)

    fun deleteRoi(dir: File) = flow {
        emit(datasource.deleteRoi(dir))
    }.flowOn(ioDispatcher)
}