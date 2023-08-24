package org.blueventures.gemdroid.model.roi

import com.github.zibnix.droidbones.mvvm.IORepository
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.data.DrawPolygon
import java.io.File

class RoiRepository(
    private val datasource: RoiDatasource = RoiDatasource(),
): IORepository() {
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
    fun addPoint(polygon: DrawPolygon, point: LatLng) = goFlow { datasource.addPoint(polygon, point) }
}