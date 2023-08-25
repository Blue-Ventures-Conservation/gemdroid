package org.blueventures.gemdroid.model.analysis.dynamics

import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.MD5
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File

class DynamicsDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    suspend fun getDynamics(dynamicsROI: DynamicsROI) = api.dynamics(dynamicsROI)
    fun saveDynamicsFile(classDir: File, urls: DynamicsURLs) = DynamicsURLs.toFile(File(classDir, dynamicsURLsFile), urls)
    fun loadDynamicsFile(classDir: File) = DynamicsURLs.fromFile(File(classDir, dynamicsURLsFile))
    fun gainTileDir(classDir: File) = File(classDir, gainTilesDir)
    fun lossTileDir(classDir: File) = File(classDir, lossTilesDir)
    fun persistenceTileDir(classDir: File) = File(classDir, persistenceTilesDir)

    companion object {
        const val dynamicsDir = "dynamics"
        const val gainTilesDir = "gain_tiles"
        const val lossTilesDir = "loss_tiles"
        const val persistenceTilesDir = "persistence_tiles"
        const val dynamicsURLsFile = "urls.json"
        const val maxSubRegions = 5

        fun classDir(roiDir: File, targetClass: String) = File(File(roiDir, dynamicsDir), MD5.string(targetClass))
    }
}