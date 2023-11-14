package org.blueventures.gemdroid.model.analysis.dynamics

import android.net.Uri
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsExports
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.dynamicDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.exportsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.gainTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.lossTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.maxSubRegions
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.persistenceTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.resultsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.subRegionsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.urlsFile
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.SubRegionsOption
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.toHexString
import java.io.File

class DynamicsViewModel(
    private val repo: DynamicsRepository = DynamicsRepository()
): Downloads.VisualizeHolder, ApiViewModel(repo), Polygons.Model {
    override var visualizer: Visualize.Visualizer? = null
    lateinit var craAwaiter: Await.CRAAwaiter
    lateinit var cra: CRA
    lateinit var targetClass: String
    lateinit var roiDir: File
    lateinit var roi: ROI
    lateinit var urls: DynamicsURLs

    override var polygonName = ""
    override var drawer = PolygonDrawer(background = ::background)
    override var shapefile: List<List<LatLng>> = emptyList()

    override val polygons = mutableListOf<PolygonDrawer.NamedPolygon>()
    override var visualize = true

    fun init(awaiter: Await.CRAAwaiter, vis: Visualize.Visualizer) {
        craAwaiter = awaiter
        visualizer = vis
    }

    private var dynamicsJob: Job? = null
    private var exportsJobs: Job? = null
    private var statusJob: Job? = null
    private var lossUriJob: Job? = null
    private var persistenceUriJob: Job? = null
    private var gainUriJob: Job? = null

    override fun displayRegions(callback: (List<List<List<LatLng>>>) -> Unit) {
        background({
            val polys = mutableListOf<List<List<LatLng>>>()
            for (poly in polygons) {
                polys.add(GeojsonPolygon.toState(poly.polygon))
            }
            polys
        }, callback)
    }

    fun classDir() = DynamicsDatasource.classDir(roiDir, targetClass)
    fun tileDirs() = listOf(lossTileDir(roiDir, targetClass), persistenceTileDir(roiDir, targetClass), gainTileDir(roiDir, targetClass))

    private fun loadSubRegionsFile(callback: (Result<DrawnPolygonsFile>) -> Unit) = loadFile(subRegionsFile(roiDir), DrawnPolygonsFile.Companion, callback)
    fun saveSubRegionsFile() = saveFile(subRegionsFile(roiDir), DrawnPolygonsFile(polygons), DrawnPolygonsFile.Companion)
    override fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = scoped { repo.validateShapefile(dynamicDir(roiDir), streams.streams, streams.names).collect(callback) }

    override fun validatePolygonName(): Boolean {
        for (region in polygons) {
            if (region.name == polygonName) {
                return false
            }
        }

        return Regexp.subRegionName.matches(polygonName)
    }

    override val named = true

    override fun polygonDrawn() {
        polygons.add(PolygonDrawer.NamedPolygon(polygonName, GeojsonPolygon.fromState(listOf(drawer.points()))))
        drawer.clear()
        polygonName = ""
    }

    override fun shapefileLooksGood() {
        polygons.add(PolygonDrawer.NamedPolygon(polygonName, GeojsonPolygon.fromState(shapefile)))
        shapefile = emptyList()
        polygonName = ""
    }

    override fun center() = Bounds.centerFromList(roi.polygonToState())
    override val storage = Maps.Storage.fromViewModel(this)
    override fun appBarTitle(title: String) = roi.appBarTitle(title)
    override val appBarTitleId = R.string.dynamics
    override val polygonType = R.string.sub_region
    override val maxPolygons = maxSubRegions
    override fun loadDrawnPolygonsFile(callback: (Result<DrawnPolygonsFile>) -> Unit) = loadSubRegionsFile(callback)
    override val polygonTypePlural = R.string.sub_regions
    override val optionsInit = SubRegionsOption.screen(this)

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
        polygons,
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

    fun clearExports() {
        deleteFile(resultsFile(roiDir, targetClass))
        deleteFile(exportsFile(roiDir, targetClass))
    }
}