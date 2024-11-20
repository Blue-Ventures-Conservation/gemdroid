package org.blueventures.gemdroid.model.analysis.dynamics

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.dynamics.ClassDynamics
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsExports
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsReady
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsReadyResponse
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.analysis.dynamics.RegionStats
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.dynamicDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.exportsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.gainTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.lossTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.maxSubRegions
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.persistenceTileDir
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.readyFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.resultsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.subRegionsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.urlsFile
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxNameCharLength
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.SubRegionsOption
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.theme.BVGreen
import org.blueventures.gemdroid.ui.theme.Chartreuse
import org.blueventures.gemdroid.ui.theme.Clear
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.toHexString
import java.io.File

class DynamicsViewModel(
    private val repo: DynamicsRepository = DynamicsRepository()
): ApiViewModel(repo), Polygons.Model {
    override var visualizer: Visualize.Visualizer? = null
    lateinit var craAwaiter: Await.CRAAwaiter
    lateinit var cra: CRA
    lateinit var targetClasses: List<String>
    lateinit var roiDir: File
    lateinit var roi: ROI
    lateinit var urls: DynamicsURLs
    lateinit var analysisRegion: RegionStats
    lateinit var analysisClass: ClassDynamics
    var alreadyDownloaded = false
    var combinedName: String? = null

    override var polygonName = ""
    override var drawer = PolygonDrawer(background = ::background)
    override var shapefile: List<List<List<LatLng>>> = emptyList()

    override val polygons = mutableListOf<PolygonDrawer.NamedPolygon>()

    fun init(awaiter: Await.CRAAwaiter, vis: Visualize.Visualizer) {
        craAwaiter = awaiter
        visualizer = vis
    }

    var subregionsFinalized = false
    private var readyJob: Job? = null
    private var dynamicsJob: Job? = null
    private var exportsJobs: Job? = null
    private var statusJob: Job? = null
    private var areasCSVJob: Job? = null
    private var lossUriJob: Job? = null
    private var persistenceUriJob: Job? = null
    private var gainUriJob: Job? = null

    override fun polygonGroups(context: Context, callback: (List<Poly.PolygonGroup>) -> Unit): Job {
        val coarseBoundaryTitle = context.getString(R.string.coarse_boundary)
        val subRegionsTitle = context.getString(R.string.sub_regions)
        return background({
            val boundary = Poly.PolygonGroup(coarseBoundaryTitle, listOf(Poly.NamedPoly(roi.name, roi.boundaryPolyToState())), Clear.toArgb(), dashes = true)
            val polys = mutableListOf<Poly.NamedPoly>()
            for (poly in polygons) polys.add(Poly.NamedPoly(poly.name, GeojsonMultiPolygon.toState(poly.polygon)))
            listOf(boundary, Poly.PolygonGroup(subRegionsTitle, polys, Clear.toArgb(), 0x7FB4B4B4, true))
        }, callback)
    }

    fun loadClassificationFile(callback: (Result<ClassificationURLs>) -> Unit) = loadFile(ClassificationDatasource.urlsFile(roiDir), ClassificationURLs.Companion, callback)
    fun classDir() = DynamicsDatasource.classDir(roiDir, targetClasses)
    fun tileDirs() = listOf(lossTileDir(roiDir, targetClasses), persistenceTileDir(roiDir, targetClasses), gainTileDir(roiDir, targetClasses))

    private fun loadSubRegionsFile(callback: (Result<DrawnPolygonsFile>) -> Unit) = loadFile(subRegionsFile(roiDir), DrawnPolygonsFile.Companion, callback)
    fun saveSubRegionsFile() = saveFile(subRegionsFile(roiDir), DrawnPolygonsFile(polygons), DrawnPolygonsFile.Companion)
    override fun validateShapefile(streams: Shapefile.Streams, callback: (Result<MultiPolyPts>) -> Unit) = scoped { repo.validateShapefile(dynamicDir(roiDir), streams.streams, streams.names).collect(callback) }

    override fun validatePolygonName(): Boolean {
        for (region in polygons) {
            if (region.name == polygonName) {
                return false
            }
        }

        return polygonName.length <= maxNameLength && Regexp.subRegionName.matches(polygonName)
    }

    override val named = true
    override fun goBack() {}

    override fun polygonDrawn() {
        polygons.add(PolygonDrawer.NamedPolygon(polygonName, GeojsonMultiPolygon.fromState(listOf(listOf(drawer.points())))))
        drawer.clear()
        polygonName = ""
    }

    override fun shapefileLooksGood() {
        polygons.add(PolygonDrawer.NamedPolygon(polygonName, GeojsonMultiPolygon.fromState(shapefile)))
        shapefile = emptyList()
        polygonName = ""
    }

    override fun center() = Bounds.centerFromMultiPoly(roi.boundaryPolyToState())
    override val storage = Maps.Storage.fromViewModel(this)
    override val shpColor = SkyBlue.toArgb()
    override fun appBarTitle(title: String) = roi.appBarTitle(title)
    override val appBarTitleId = R.string.dynamics
    override val maxNameLength = maxNameCharLength
    override val polygonType = R.string.sub_region
    override val attemptGps = false
    override val maxPolygons = maxSubRegions
    override fun loadDrawnPolygonsFile(callback: (Result<DrawnPolygonsFile>) -> Unit) = loadSubRegionsFile(callback)
    override val polygonTypePlural = R.string.sub_regions

    override fun edit() = false
    override fun clearEdit() {}

    fun combinedNameNeeded() = !alreadyDownloaded && targetClasses.size > 1
    fun validateCombinedName(name: String): Boolean {
        if (name.length > maxNameLength || !Regexp.roiName.matches(name)) {
            return false
        }

        for (className in cra.contemporaryCRA.stringClassValues) {
            if (name == className) {
                return false
            }
        }

        return true
    }

    private var contOp: String? = null
    private var histOp: String? = null
    @Composable
    override fun optionsInit(snack: SnackFun, back: Click, content: @Composable () -> Unit) {
        contOp = null
        histOp = null

        SubRegionsOption.Screen(this, snack, back, content)

        BackHandler {
            contOp = null
            histOp = null
            back()
        }
    }
    fun getDynamicsReady(callback: (ApiResult<DynamicsReadyResponse>) -> Unit) {
        if (contOp == null || histOp == null) {
            loadClassificationFile { res ->
                if (res.isSuccess) {
                    val urls = res.getOrNull()!!
                    contOp = urls.contemporaryClassification.imageOp
                    histOp = urls.historicalClassification.imageOp
                }
                fetchDynamicsReady(callback)
            }
        } else {
            fetchDynamicsReady(callback)
        }
    }
    private fun fetchDynamicsReady(callback: (ApiResult<DynamicsReadyResponse>) -> Unit) {
        readyJob = getRemote(readyJob, makeDynamicsReady(contOp ?: "unknown", histOp ?: "unknown"), callback) { api, ready ->
            api.dynamicsReady(ready)
        }
    }
    fun saveDynamicsReadyFile(ready: DynamicsReadyResponse) = saveFile(readyFile(roiDir), ready, DynamicsReadyResponse.Companion)
    fun loadDynamicsReadyFile(callback: (Result<DynamicsReadyResponse>) -> Unit) = loadFile(readyFile(roiDir), DynamicsReadyResponse.Companion) { result ->
        when {
            result.isSuccess -> {
                val resp = result.getOrNull()!!
                contOp = resp.contOp
                histOp = resp.histOp

                if (!resp.isReady()) {
                    callback(Result.failure(Throwable()))
                } else {
                    callback(result)
                }
            }
            else -> callback(result)
        }
    }

    fun getDynamics(callback: (ApiResult<DynamicsURLs>) -> Unit) {
        dynamicsJob = getRemote(dynamicsJob, makeDynamicsROI(), callback) { api, roi ->
            api.dynamics(roi)
        }
    }
    fun saveDynamicsFile(urls: DynamicsURLs) = saveFile(urlsFile(roiDir, targetClasses), urls, DynamicsURLs.Companion)
    fun loadDynamicsFile(callback: (Result<DynamicsURLs>) -> Unit) = loadFile(urlsFile(roiDir, targetClasses), DynamicsURLs.Companion, callback)

    fun saveExports(exports: DynamicsExports) = saveFile(exportsFile(roiDir, targetClasses), exports, DynamicsExports.Companion)
    fun loadExports(callback: (Result<DynamicsExports>) -> Unit) = loadFile(exportsFile(roiDir, targetClasses), DynamicsExports.Companion, callback)
    fun getExports(callback: (ApiResult<DynamicsExports>) -> Unit) {
        exportsJobs = getRemote(exportsJobs, makeDynamicsROI(), { result ->
            if (result is ApiResult.Success) {
                deleteFile(resultsFile(roiDir, targetClasses)) { callback(result) }
            } else {
                callback(result)
            }
        }) { api, roi ->
            api.exportDynamics(roi)
        }
    }

    private fun makeDynamicsReady(contOp: String, histOp: String) = DynamicsReady(
        contOp,
        histOp,
        cra.contemporaryCRA.shapefileStorageKey,
        cra.historicalShp().shapefileStorageKey,
        cra.useContSpec(),
        cra.contemporaryCRA.numericClassField,
        cra.contemporaryCRA.stringClassField,
        roi,
    )

    private fun makeDynamicsROI() = DynamicsROI(
        targetClasses,
        combinedName,
        polygons,
        MildRed.toHexString(),
        BVGreen.toHexString(),
        Chartreuse.toHexString(),
        cra.contemporaryCRA.shapefileStorageKey,
        cra.historicalShp().shapefileStorageKey,
        cra.useContSpec(),
        cra.contemporaryCRA.numericClassField,
        cra.contemporaryCRA.stringClassField,
        roi,
    )

    fun saveResults(results: TasksResults) = saveResults(resultsFile(roiDir, targetClasses), results)
    fun loadResults(callback: (Result<TasksResults>) -> Unit) = loadResults(resultsFile(roiDir, targetClasses), callback)
    fun getResults(exports: DynamicsExports, callback: (ApiResult<TasksResults>) -> Unit) {
        val taskList = mutableListOf<String>()

        if (exports.csv != null) {
            taskList.add(exports.csv.name)
        }

        taskList.addAll(listOf(exports.loss.name, exports.persistence.name, exports.gain.name))

        statusJob = getRemote(statusJob, Tasks(taskList), callback) { api, tasks ->
            api.tasksResults(tasks)
        }
    }

    fun getAreasCSVUri(path: String, callback: (Result<Uri>) -> Unit) {
        areasCSVJob = uriFromStorage(areasCSVJob, path, callback)
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
        deleteFile(resultsFile(roiDir, targetClasses))
        deleteFile(exportsFile(roiDir, targetClasses))
    }
}