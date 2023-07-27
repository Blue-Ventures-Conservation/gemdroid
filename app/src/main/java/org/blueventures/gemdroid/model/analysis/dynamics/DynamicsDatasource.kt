package org.blueventures.gemdroid.model.analysis.dynamics

import org.blueventures.gemdroid.api.Api
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
    fun saveDynamicsFile(roiDir: File, urls: DynamicsURLs) = DynamicsURLs.toFile(File(classDir(roiDir), dynamicsURLsFile), urls)
    fun loadDynamicsFile(roiDir: File) = DynamicsURLs.fromFile(File(classDir(roiDir), dynamicsURLsFile))
    fun gainTileDir(roiDir: File) = File(File(roiDir, dynamicsDir), gainTilesDir)
    fun lossTileDir(roiDir: File) = File(File(roiDir, dynamicsDir), lossTilesDir)
    fun persistenceTileDir(roiDir: File) = File(File(roiDir, dynamicsDir), persistenceTilesDir)

    private fun classDir(roiDir: File) = File(roiDir, dynamicsDir)

    companion object {
        const val dynamicsDir = "classification"
        const val gainTilesDir = "gain_tiles"
        const val lossTilesDir = "loss_tiles"
        const val persistenceTilesDir = "persistence_tiles"
        const val dynamicsURLsFile = "urls.json"
    }
}