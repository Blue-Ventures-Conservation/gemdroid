package org.blueventures.gemdroid.model.analysis.dynamics

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.MD5
import org.blueventures.gemdroid.data.Shapefile.polygons
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegion
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegionsFile
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File
import java.io.InputStream

class DynamicsDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    fun loadSubRegionsFile(classDir: File) = SubRegionsFile.fromFile(File(classDir, subRegionFile))
    fun saveSubRegionsFile(classDir: File, regions: List<SubRegion>) = SubRegionsFile.toFile(File(classDir, subRegionFile), SubRegionsFile(regions))
    suspend fun getDynamics(dynamicsROI: DynamicsROI) = api.dynamics(dynamicsROI)
    fun saveDynamicsFile(classDir: File, urls: DynamicsURLs) = DynamicsURLs.toFile(File(classDir, dynamicsURLsFile), urls)
    fun loadDynamicsFile(classDir: File) = DynamicsURLs.fromFile(File(classDir, dynamicsURLsFile))
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = adder(point)
    fun validateShapefile(classDir: File, files: List<InputStream?>, names: List<String?>) = polygons(classDir, files, names)

    fun gainTileDir(classDir: File) = File(classDir, gainTilesDir)
    fun lossTileDir(classDir: File) = File(classDir, lossTilesDir)
    fun persistenceTileDir(classDir: File) = File(classDir, persistenceTilesDir)

    companion object {
        const val dynamicsDir = "dynamics"
        const val subRegionFile = "subregions.json"
        const val gainTilesDir = "gain_tiles"
        const val lossTilesDir = "loss_tiles"
        const val persistenceTilesDir = "persistence_tiles"
        const val dynamicsUnzipDir = "unzip"
        const val dynamicsURLsFile = "urls.json"
        const val maxSubRegions = 10

        fun classDir(roiDir: File, targetClass: String) = File(File(roiDir, dynamicsDir), MD5.string(targetClass))
    }
}