package org.blueventures.gemdroid.model.analysis.dynamics

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File

class DynamicsViewModel(
    private val repo: DynamicsRepository = DynamicsRepository()
): ApiViewModel(repo) {
    lateinit var craAwaiter: CRAAwaiter

    var roiDir = File("")
        set(value) {
            field = value
            setTileDirs()
        }

    var roi = ROI()
        set(value) {
            field = value
        }

    fun init(awaiter: CRAAwaiter) {
        craAwaiter = awaiter
    }

    private var dynamicsJob: Job? = null

    var tileDirs: List<File> = listOf()
    private fun setTileDirs() {
        tileDirs = listOf(repo.gainTileDir(roiDir), repo.lossTileDir(roiDir), repo.persistenceTileDir(roiDir))
    }

    fun getDynamics(cra: CRA, callback: (ApiResult<DynamicsURLs>) -> Unit) {
        if (dynamicsJob != null) return
        apiWithToken({ dynamicsJob = it }, repo.getDynamics(DynamicsROI(
            cra.contemporaryCRA.shapefileStorageKey,
            cra.historicalShp().shapefileStorageKey,
            cra.useContSpec(),
            cra.contemporaryCRA.numericClassField,
            cra.contemporaryCRA.stringClassField,
            roi,
        )
        )) { result ->
            dynamicsJob = null
            callback(result)
        }
    }
    fun saveDynamicsFile(urls: DynamicsURLs) = scoped { repo.saveDynamicsFile(roiDir, urls).collect() }
    fun loadDynamicsFile(callback: (Result<DynamicsURLs>) -> Unit) = scoped { repo.loadDynamicsFile(roiDir).collect(callback) }
}