package org.blueventures.gemdroid.model.analysis.dynamics

import android.net.Uri
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.DrawPolygon
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsExports
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegion
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegionsFile
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.dynamicDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.exportsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.gainTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.lossTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.persistenceTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.resultsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.subRegionsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.urlsFile
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.toHexString
import java.io.File

class DynamicsViewModel(
    private val repo: DynamicsRepository = DynamicsRepository()
): Downloads.VisualizeHolder, ApiViewModel(repo) {
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
    override var visualize = true

    fun init(awaiter: CRAAwaiter, vis: Visualize.Visualizer) {
        craAwaiter = awaiter
        visualizer = vis
    }

    private var dynamicsJob: Job? = null
    private var exportsJobs: Job? = null
    private var statusJob: Job? = null
    private var lossUriJob: Job? = null
    private var persistenceUriJob: Job? = null
    private var gainUriJob: Job? = null

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

    fun saveExports(exports: DynamicsExports) = saveFile(exportsFile(roiDir, targetClass), exports, DynamicsExports.Companion)
    fun loadExports(callback: (Result<DynamicsExports>) -> Unit) = loadFile(exportsFile(roiDir, targetClass), DynamicsExports.Companion, callback)
    fun getExports(visualize: Boolean, callback: (ApiResult<DynamicsExports>) -> Unit) {
        exportsJobs = getRemote(exportsJobs, makeDynamicsROI(visualize), { result ->
            if (result is ApiResult.Success) {
                deleteFile(resultsFile(roiDir, targetClass)) { callback(result) }
            } else {
                callback(result)
            }
        }) { api, roi ->
            api.exportDynamics(roi)
        }
    }

    private fun makeDynamicsROI(visualize: Boolean = true) = DynamicsROI(
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
        roi.copy(visualize = visualize),
    )

    fun saveResults(results: TasksResults) = saveResults(resultsFile(roiDir, targetClass), results)
    fun loadResults(callback: (Result<TasksResults>) -> Unit) = loadResults(resultsFile(roiDir, targetClass), callback)
    fun getResults(exports: DynamicsExports, callback: (ApiResult<TasksResults>) -> Unit) {
        statusJob = getRemote(statusJob, Tasks(listOf(exports.loss.name, exports.persistence.name, exports.gain.name)), callback) { api, tasks ->
            api.tasksResults(tasks)
        }
    }

    fun getLossUri(path: String, callback: (Result<Uri>) -> Unit) {
        lossUriJob = uriFromStorage(lossUriJob, path, callback)
    }
    fun getPersistenceUri(path: String, callback: (Result<Uri>) -> Unit) {
        persistenceUriJob = uriFromStorage(persistenceUriJob, path, callback)
    }
    fun getGainUri(path: String, callback: (Result<Uri>) -> Unit) {
        gainUriJob = uriFromStorage(gainUriJob, path, callback)
    }
}