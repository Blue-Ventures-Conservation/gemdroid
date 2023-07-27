package org.blueventures.gemdroid.model.analysis.dynamics

import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class DynamicsRepository(
    private val datasource: DynamicsDatasource = DynamicsDatasource(),
): ApiRepository(datasource) {
    fun getDynamics(dynamicsROI: DynamicsROI) = goFlow { datasource.getDynamics(dynamicsROI) }
    fun saveDynamicsFile(roiDir: File, urls: DynamicsURLs) = goFlow { datasource.saveDynamicsFile(roiDir, urls) }
    fun loadDynamicsFile(roiDir: File) = goFlow { datasource.loadDynamicsFile(roiDir) }
    fun gainTileDir(roiDir: File) = datasource.gainTileDir(roiDir)
    fun lossTileDir(roiDir: File) = datasource.lossTileDir(roiDir)
    fun persistenceTileDir(roiDir: File) = datasource.persistenceTileDir(roiDir)
}