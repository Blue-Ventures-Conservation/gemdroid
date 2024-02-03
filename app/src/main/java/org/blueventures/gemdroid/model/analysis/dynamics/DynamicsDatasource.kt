package org.blueventures.gemdroid.model.analysis.dynamics

import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.MD5
import org.blueventures.gemdroid.data.Shapefile.Companion.polygons
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File
import java.io.InputStream

class DynamicsDatasource(
    api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    fun validateShapefile(dynamicsDir: File, files: List<InputStream?>, names: List<String?>) = polygons(dynamicsDir, files, names)

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
        fun classDir(roiDir: File, targetClasses: List<String>) = File(dynamicDir(roiDir), MD5.string(targetClasses.reduce { acc, className -> acc + className }))
        fun urlsFile(roiDir: File, targetClasses: List<String>) = File(classDir(roiDir, targetClasses), dynamicsURLsFile)
        fun exportsFile(roiDir: File, targetClasses: List<String>) = File(classDir(roiDir, targetClasses), exportsFilename)
        fun resultsFile(roiDir: File, targetClasses: List<String>) = File(classDir(roiDir, targetClasses), resultsFile)
        fun gainTileDir(roiDir: File, targetClasses: List<String>) = File(classDir(roiDir, targetClasses), gainTilesDir)
        fun lossTileDir(roiDir: File, targetClasses: List<String>) = File(classDir(roiDir, targetClasses), lossTilesDir)
        fun persistenceTileDir(roiDir: File, targetClasses: List<String>) = File(classDir(roiDir, targetClasses), persistenceTilesDir)
    }
}