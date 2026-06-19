package org.blueventures.gemdroid.model.analysis

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.ui.graphics.toArgb
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.analysis.CompositesAssessed
import org.blueventures.gemdroid.data.analysis.ImageryExports
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.chotTileDir
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.clotTileDir
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.compositesAssessedFile
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.exportsFile
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.hhotTileDir
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.hlotTileDir
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.resultsFile
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.roiFile
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.urlsFile
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.theme.MildRed
import java.io.File

class AnalysisViewModel(
    private val repo: AnalysisRepository = AnalysisRepository()
): Visualize.Visualizer, Downloads.VisualizeHolder, ApiViewModel(repo) {
    lateinit var craViewModel: CRAViewModel
    lateinit var classViewModel: ClassificationViewModel
    lateinit var dynamicsViewModel: DynamicsViewModel

    var roiDir = File("")
        set(value) {
            field = value
            craViewModel.roiDir = value
            classViewModel.roiDir = value
            dynamicsViewModel.roiDir = value
        }

    var roi = ROI()
        set(value) {
            field = value
            classViewModel.roi = value
            dynamicsViewModel.roi = value
        }

    var stage = Stage.COMPOSITES
    override var visualize = true

    fun init(activity: ComponentActivity) {
        craViewModel = activity.viewModels<CRAViewModel>().value
        classViewModel = activity.viewModels<ClassificationViewModel>().value
        classViewModel.init(activity, craViewModel)
        dynamicsViewModel = activity.viewModels<DynamicsViewModel>().value
        dynamicsViewModel.init(craViewModel, this)
    }

    private var visualizeURLsJob: Job? = null
    private var exportsJob: Job? = null
    private var statusJob: Job? = null
    private var chotUriJob: Job? = null
    private var clotUriJob: Job? = null
    private var hhotUriJob: Job? = null
    private var hlotUriJob: Job? = null

    private fun tileDirs() = listOf(chotTileDir(roiDir), clotTileDir(roiDir), hhotTileDir(roiDir), hlotTileDir(roiDir))

    fun refreshStage(callback: (Stage) -> Unit) = scoped { repo.getStage(roiDir).collect(callback) }
    fun getROI(callback: (Result<ROI>) -> Unit) = loadFile(roiFile(roiDir), ROI.Companion, callback)
    fun saveROI(roi: ROI) = saveFile(roiFile(roiDir), roi, ROI.Companion)

    override fun parentDir() = roiDir
    override fun tileDir(i: Int) = tileDirs()[i]

    override fun saveVisualizeURLsFile(urls: VisualizeURLs): Job {
        scoped { repo.makeVisualizeTileDirs(roiDir).collect() }
        return saveFile(urlsFile(roiDir), urls, VisualizeURLs.Companion)
    }
    override fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit) = loadFile(urlsFile(roiDir), VisualizeURLs.Companion, callback)
    override fun getVisualizeURLs(callback: (ApiResult<VisualizeURLs>) -> Unit) {
        visualizeURLsJob = getRemote(visualizeURLsJob, roi, { apiResult ->
            if (apiResult is ApiResult.Success) {
                val buffer = apiResult.data!!.buffDist
                roi = roi.copy(buffDist = buffer)
                saveFile(roiFile(roiDir), roi, ROI.Companion) {
                    callback(apiResult)
                }
            } else {
                callback(apiResult)
            }
        }) { api, roi ->
            api.getVisualizeURLs(roi)
        }
    }

    fun saveExports(exports: ImageryExports) = saveFile(exportsFile(roiDir), exports, ImageryExports.Companion)
    fun loadExports(callback: (Result<ImageryExports>) -> Unit) = loadFile(exportsFile(roiDir), ImageryExports.Companion, callback)
    fun getExports(visualize: Boolean, callback: (ApiResult<ImageryExports>) -> Unit) {
        exportsJob = getRemote(exportsJob, roi.copy(visualize = visualize), { result ->
            if (result is ApiResult.Success) {
                scoped { deleteFile(resultsFile(roiDir)) { callback(result) } }
            } else {
                callback(result)
            }
        }) { api, roi ->
            api.exportLandsat(roi)
        }
    }

    fun saveResults(results: TasksResults) = saveResults(resultsFile(roiDir), results)
    fun loadResults(callback: (Result<TasksResults>) -> Unit) = loadResults(resultsFile(roiDir), callback)
    fun getResults(exports: ImageryExports, callback: (ApiResult<TasksResults>) -> Unit) {
        statusJob = getRemote(statusJob, Tasks(listOf(exports.chot.name, exports.clot.name, exports.hhot.name, exports.hlot.name)), callback) { api, tasks ->
            api.tasksResults(tasks)
        }
    }

    fun getChotUri(path: String, callback: (Result<Uri>) -> Unit) {
        chotUriJob = uriFromStorage(chotUriJob, path, callback)
    }
    fun getClotUri(path: String, callback: (Result<Uri>) -> Unit) {
        clotUriJob = uriFromStorage(clotUriJob, path, callback)
    }
    fun getHhotUri(path: String, callback: (Result<Uri>) -> Unit) {
        hhotUriJob = uriFromStorage(hhotUriJob, path, callback)
    }
    fun getHlotUri(path: String, callback: (Result<Uri>) -> Unit) {
        hlotUriJob = uriFromStorage(hlotUriJob, path, callback)
    }

    fun clearExports() {
        deleteFile(resultsFile(roiDir))
        deleteFile(exportsFile(roiDir))
    }

    fun saveCompositesAssessedFile() = saveFile(compositesAssessedFile(roiDir), CompositesAssessed(true), CompositesAssessed.Companion)
    fun deleteComposites(callback: (Result<Unit>) -> Unit) = scoped { repo.deleteComposites(roiDir).collect(callback) }

    fun excludedRegions(context: Context, startVisible: Boolean = true, callback: (Poly.PolygonGroup?) -> Unit): Job {
        val excludedRegionsTitle = context.getString(R.string.excluded_regions)
        return if (roi.excludedRegions.isNotEmpty()) {
            val excludes = roi.excludedRegions
            background({
                val polys = mutableListOf<Poly.NamedPoly>()
                for (poly in excludes) polys.add(Poly.NamedPoly("", GeojsonMultiPolygon.toState(poly)))
                Poly.PolygonGroup(excludedRegionsTitle, polys, MildRed.toArgb(), startVisible = startVisible)
            }, callback)
        } else {
            background({ null }, callback)
        }
    }

    fun backgroundPolygon(context: Context, startVisible: Boolean = true, callback: (Poly.PolygonGroup) -> Unit) = background({ backgroundPolygon(context, startVisible) }, callback)

    private fun backgroundPolygon(context: Context, startVisible: Boolean = true) = Poly.PolygonGroup(context.getString(R.string.coarse_boundary), listOf(Poly.NamedPoly(roi.name, roi.boundaryPolyToState())), startVisible = startVisible)
}