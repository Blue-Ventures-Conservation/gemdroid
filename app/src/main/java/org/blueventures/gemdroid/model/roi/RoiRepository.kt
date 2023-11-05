package org.blueventures.gemdroid.model.roi

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class RoiRepository(
    private val datasource: RoiDatasource = RoiDatasource(),
): ApiRepository() {
    fun getRois(filesDir: File) = goFlow { datasource.getRois(filesDir) }
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
        points: List<LatLng>
    ) = goFlow{ datasource.saveRoi(filesDir, name, contYearStart, contYearEnd, contMonthStart, contMonthEnd, histYearStart, histYearEnd, histMonthStart, histMonthEnd, points) }
    fun deleteRoi(dir: File) = goFlow { datasource.deleteRoi(dir) }
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = goFlow { adder(point) }
}