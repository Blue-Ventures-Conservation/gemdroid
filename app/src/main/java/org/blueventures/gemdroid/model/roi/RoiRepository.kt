package org.blueventures.gemdroid.model.roi

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

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
        points: List<LatLng>,
        excludes: List<PolygonDrawer.NamedPolygon>
    ) = goFlow{ datasource.saveRoi(filesDir, name, contYearStart, contYearEnd, contMonthStart, contMonthEnd, histYearStart, histYearEnd, histMonthStart, histMonthEnd, points, excludes) }
    fun deleteRoi(dir: File) = goFlow { datasource.deleteRoi(dir) }
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = goFlow { adder(point) }
    fun validateShapefile(roiDir: File, files: List<InputStream?>, names: List<String?>) = goFlow { datasource.validateShapefile(roiDir, files, names) }
}