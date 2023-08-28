package org.blueventures.gemdroid.model.analysis.dynamics

import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.DrawPolygon
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegion
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegionsFile
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.dynamicsDir
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.toHexString
import java.io.File

class DynamicsViewModel(
    private val repo: DynamicsRepository = DynamicsRepository()
): ApiViewModel(repo) {
    lateinit var craAwaiter: CRAAwaiter
    lateinit var visualizer: Visualize.Visualizer
    lateinit var cra: CRA
    lateinit var targetClass: String
    lateinit var roiDir: File
    lateinit var roi: ROI
    lateinit var urls: DynamicsURLs

    var regionName = ""
    var drawPoly = DrawPolygon(null) { point, adder, callback ->
        scoped { repo.addPoint(point, adder).collect(callback) }
    }
    var shapefile: List<List<LatLng>> = emptyList()

    val subRegions = mutableListOf<SubRegion>()
    var subRegionsLoaded = false

    fun init(awaiter: CRAAwaiter, vis: Visualize.Visualizer) {
        craAwaiter = awaiter
        visualizer = vis
    }

    private var dynamicsJob: Job? = null

    fun classDir() = DynamicsDatasource.classDir(roiDir, targetClass)
    fun tileDirs() = listOf(repo.lossTileDir(classDir()), repo.persistenceTileDir(classDir()), repo.gainTileDir(classDir()))

    fun loadSubRegionsFile(callback: (Result<SubRegionsFile>) -> Unit) = scoped { repo.loadSubRegionsFile(File(roiDir, dynamicsDir)).collect(callback) }
    fun saveSubRegionsFile() = scoped { repo.saveSubRegionsFile(File(roiDir, dynamicsDir), subRegions).collect() }

    fun validateRegionName() = Regexp.subRegionName.matches(regionName)
    fun polygonDrawn() {
        subRegions.add(SubRegion(regionName, GeojsonPolygon.fromState(listOf(drawPoly.points))))
        drawPoly.clearPoints()
    }
    fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = scoped { repo.validateShapefile(classDir(), streams.streams, streams.names).collect(callback) }
    fun shapefileLooksGood() {
        subRegions.add(SubRegion(regionName, GeojsonPolygon.fromState(shapefile)))
        shapefile = emptyList()
    }

    fun getDynamics(callback: (ApiResult<DynamicsURLs>) -> Unit) {
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
    fun saveDynamicsFile(urls: DynamicsURLs) = scoped { repo.saveDynamicsFile(classDir(), urls).collect() }
    fun loadDynamicsFile(callback: (Result<DynamicsURLs>) -> Unit) = scoped { repo.loadDynamicsFile(classDir()).collect(callback) }
}