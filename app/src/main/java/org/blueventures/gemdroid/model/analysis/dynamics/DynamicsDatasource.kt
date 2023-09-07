package org.blueventures.gemdroid.model.analysis.dynamics

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.MD5
import org.blueventures.gemdroid.data.Shapefile.polygons
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File
import java.io.InputStream

class DynamicsDatasource(
    api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    fun validateShapefile(dynamicsDir: File, files: List<InputStream?>, names: List<String?>) = polygons(dynamicsDir, files, names)
    fun addPoint(point: LatLng, adder: (LatLng) -> Unit) = adder(point)

    companion object {
        private const val dynamicsDir = "dynamics"
        private const val subRegionFile = "subregions.json"
        private const val gainTilesDir = "gain_tiles"
        private const val lossTilesDir = "loss_tiles"
        private const val persistenceTilesDir = "persistence_tiles"
        private const val dynamicsURLsFile = "urls.json"
        private const val exportsFilename = "dynamics_exports.json"
        private const val resultsFile = "results.json"
        const val maxSubRegions = 5

        fun dynamicDir(roiDir: File) = File(roiDir, dynamicsDir)
        fun subRegionsFile(roiDir: File) = File(dynamicDir(roiDir), subRegionFile)
        fun classDir(roiDir: File, targetClass: String) = File(dynamicDir(roiDir), MD5.string(targetClass))
        fun urlsFile(roiDir: File, targetClass: String) = File(classDir(roiDir, targetClass), dynamicsURLsFile)
        fun exportsFile(roiDir: File, targetClass: String) = File(classDir(roiDir, targetClass), exportsFilename)
        fun resultsFile(roiDir: File, targetClass: String) = File(classDir(roiDir, targetClass), resultsFile)
        fun gainTileDir(roiDir: File, targetClass: String) = File(classDir(roiDir, targetClass), gainTilesDir)
        fun lossTileDir(roiDir: File, targetClass: String) = File(classDir(roiDir, targetClass), lossTilesDir)
        fun persistenceTileDir(roiDir: File, targetClass: String) = File(classDir(roiDir, targetClass), persistenceTilesDir)
    }
}