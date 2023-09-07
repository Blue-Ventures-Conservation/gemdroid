package org.blueventures.gemdroid.model.analysis.dynamics

import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
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
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.dynamicDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.gainTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.lossTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.persistenceTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.subRegionsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.urlsFile
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

    fun displayRegions(): List<List<List<LatLng>>> {
        val polygons = mutableListOf<List<List<LatLng>>>()
        for (region in subRegions) {
            polygons.add(GeojsonPolygon.toState(region.polygon))
        }
        return polygons
    }

    fun classDir() = DynamicsDatasource.classDir(roiDir, targetClass)
    fun tileDirs() = listOf(lossTileDir(roiDir, targetClass), persistenceTileDir(roiDir, targetClass), gainTileDir(roiDir, targetClass))

    fun loadSubRegionsFile(callback: (Result<SubRegionsFile>) -> Unit) = loadFile(subRegionsFile(roiDir), SubRegionsFile.Companion, callback)
    fun saveSubRegionsFile() = saveFile(subRegionsFile(roiDir), SubRegionsFile(subRegions), SubRegionsFile.Companion)
    fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = scoped { repo.validateShapefile(dynamicDir(roiDir), streams.streams, streams.names).collect(callback) }

    fun validateRegionName() = Regexp.subRegionName.matches(regionName)
    fun polygonDrawn() {
        subRegions.add(SubRegion(regionName, GeojsonPolygon.fromState(listOf(drawPoly.points))))
        drawPoly.clearPoints()
        regionName = ""
    }
    fun shapefileLooksGood() {
        subRegions.add(SubRegion(regionName, GeojsonPolygon.fromState(shapefile)))
        shapefile = emptyList()
        regionName = ""
    }

    fun getDynamics(callback: (ApiResult<DynamicsURLs>) -> Unit) {
        dynamicsJob = getRemote(dynamicsJob, makeDynamicsROI(), callback) { api, roi ->
            api.dynamics(roi)
        }
    }
    fun saveDynamicsFile(urls: DynamicsURLs) = saveFile(urlsFile(roiDir, targetClass), urls, DynamicsURLs.Companion)
    fun loadDynamicsFile(callback: (Result<DynamicsURLs>) -> Unit) = loadFile(urlsFile(roiDir, targetClass), DynamicsURLs.Companion, callback)

    private fun makeDynamicsROI() = DynamicsROI(
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
    )
}