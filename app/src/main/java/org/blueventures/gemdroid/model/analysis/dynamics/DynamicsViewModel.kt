package org.blueventures.gemdroid.model.analysis.dynamics

import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegion
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegionsFile
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.Visualizer
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.classDir
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.toHexString
import java.io.File

class DynamicsViewModel(
    private val repo: DynamicsRepository = DynamicsRepository()
): ApiViewModel(repo) {
    lateinit var craAwaiter: CRAAwaiter
    lateinit var visualizer: Visualizer
    lateinit var cra: CRA
    lateinit var targetClass: String
    lateinit var roiDir: File
    lateinit var roi: ROI

    var regionName = ""

    val subRegions = mutableListOf<SubRegion>()

    fun init(awaiter: CRAAwaiter, vis: Visualizer) {
        craAwaiter = awaiter
        visualizer = vis
    }

    private var dynamicsJob: Job? = null

    fun tileDirs() = listOf(repo.lossTileDir(classDir(roiDir, targetClass)), repo.persistenceTileDir(classDir(roiDir, targetClass)), repo.gainTileDir(classDir(roiDir, targetClass)))

    fun loadSubRegionsFile(callback: (Result<SubRegionsFile>) -> Unit) = scoped { repo.loadSubRegionsFile(classDir(roiDir, targetClass)).collect(callback) }
    fun saveSubRegionsFile() = scoped { repo.saveSubRegionsFile(classDir(roiDir, targetClass), subRegions).collect() }

    fun validateRegionName() = Regexp.subRegionName.matches(regionName)

    fun getDynamics(cra: CRA, callback: (ApiResult<DynamicsURLs>) -> Unit) {
        if (dynamicsJob != null) return
        apiWithToken({ dynamicsJob = it }, repo.getDynamics(DynamicsROI(
            targetClass,
            subRegions,
            MildRed.toHexString(),
            LightGreen.toHexString(),
            SkyBlue.toHexString(),
            cra.contemporaryCRA.shapefileStorageKey,
            cra.historicalShp().shapefileStorageKey,
            cra.useContSpec(),
            cra.contemporaryCRA.numericClassField,
            cra.contemporaryCRA.stringClassField,
            roi,
        ))) { result ->
            dynamicsJob = null
            callback(result)
        }
    }
    fun saveDynamicsFile(urls: DynamicsURLs) = scoped { repo.saveDynamicsFile(roiDir, urls).collect() }
    fun loadDynamicsFile(callback: (Result<DynamicsURLs>) -> Unit) = scoped { repo.loadDynamicsFile(roiDir).collect(callback) }
    fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = scoped { repo.validateShapefile(classDir(roiDir, targetClass), streams.streams, streams.names).collect() }
}