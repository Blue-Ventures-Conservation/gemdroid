package org.blueventures.gemdroid.model.analysis.dynamics

import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class DynamicsRepository(
    private val datasource: DynamicsDatasource = DynamicsDatasource(),
): ApiRepository(datasource) {
    fun getDynamics(dynamicsROI: DynamicsROI) = goFlow { datasource.getDynamics(dynamicsROI) }
    fun saveDynamicsFile(classDir: File, urls: DynamicsURLs) = goFlow { datasource.saveDynamicsFile(classDir, urls) }
    fun loadDynamicsFile(classDir: File) = goFlow { datasource.loadDynamicsFile(classDir) }
    fun gainTileDir(classDir: File) = datasource.gainTileDir(classDir)
    fun lossTileDir(classDir: File) = datasource.lossTileDir(classDir)
    fun persistenceTileDir(classDir: File) = datasource.persistenceTileDir(classDir)
}