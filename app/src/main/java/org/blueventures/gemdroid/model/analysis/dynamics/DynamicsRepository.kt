package org.blueventures.gemdroid.model.analysis.dynamics

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegion
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class DynamicsRepository(
    private val datasource: DynamicsDatasource = DynamicsDatasource(),
): ApiRepository(datasource) {
    fun loadSubRegionsFile(dynamicsDir: File) = goFlow{ datasource.loadSubRegionsFile(dynamicsDir) }
    fun saveSubRegionsFile(dynamicsDir: File, regions: List<SubRegion>) = goFlow { datasource.saveSubRegionsFile(dynamicsDir, regions) }
    fun validateShapefile(dynamicsDir: File, files: List<InputStream?>, names: List<String?>) = goFlow { datasource.validateShapefile(dynamicsDir, files, names) }
    fun getDynamics(dynamicsROI: DynamicsROI) = goFlow { datasource.getDynamics(dynamicsROI) }
    fun saveDynamicsFile(classDir: File, urls: DynamicsURLs) = goFlow { datasource.saveDynamicsFile(classDir, urls) }
    fun loadDynamicsFile(classDir: File) = goFlow { datasource.loadDynamicsFile(classDir) }
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = goFlow { datasource.addPoint(point, adder) }

    fun gainTileDir(classDir: File) = datasource.gainTileDir(classDir)
    fun lossTileDir(classDir: File) = datasource.lossTileDir(classDir)
    fun persistenceTileDir(classDir: File) = datasource.persistenceTileDir(classDir)
}