package org.blueventures.gemdroid.model.roi

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class RoiRepository(
    private val datasource: RoiDatasource = RoiDatasource(),
): ApiRepository() {
    fun getRois(filesDir: File) = goFlow { datasource.getRois(filesDir) }
    fun saveRoi(filesDir: File, roi: ROI) = goFlow { datasource.saveRoi(filesDir, roi) }
    fun deleteRoi(dir: File) = goFlow { datasource.deleteRoi(dir) }
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = goFlow { adder(point) }
    fun validateShapefile(roiDir: File, files: List<InputStream?>, names: List<String?>) = goFlow { datasource.validateShapefile(roiDir, files, names) }
}