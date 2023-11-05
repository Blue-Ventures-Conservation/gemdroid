package org.blueventures.gemdroid.model.analysis.dynamics

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class DynamicsRepository(
    private val datasource: DynamicsDatasource = DynamicsDatasource(),
): ApiRepository(datasource) {
    fun validateShapefile(dynamicsDir: File, files: List<InputStream?>, names: List<String?>) = goFlow { datasource.validateShapefile(dynamicsDir, files, names) }
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = goFlow { adder(point) }
}