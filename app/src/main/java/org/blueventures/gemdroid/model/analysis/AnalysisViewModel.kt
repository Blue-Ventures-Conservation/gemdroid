package org.blueventures.gemdroid.model.analysis

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.analysis.Buffers
import org.blueventures.gemdroid.data.analysis.ImageryExports
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.common.maps.Visualize
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

    var stage = Stage.BUFFER
    override var visualize = true

    fun init(activity: ComponentActivity) {
        craViewModel = activity.viewModels<CRAViewModel>().value
        classViewModel = activity.viewModels<ClassificationViewModel>().value
        classViewModel.init(activity, craViewModel)
        dynamicsViewModel = activity.viewModels<DynamicsViewModel>().value
        dynamicsViewModel.init(craViewModel, this)
    }

    private var buffersJob: Job? = null
    private var visualizeURLsJob: Job? = null
    private var exportsJobs: Job? = null
    private var statusJob: Job? = null
    private var chotUriJob: Job? = null
    private var clotUriJob: Job? = null
    private var hhotUriJob: Job? = null
    private var hlotUriJob: Job? = null

    private fun tileDirs() = listOf(repo.chotTileDir(roiDir), repo.clotTileDir(roiDir), repo.hhotTileDir(roiDir), repo.hlotTileDir(roiDir))

    fun refreshStage(callback: (Stage) -> Unit) = scoped { repo.getStage(roiDir).collect(callback) }
    fun getROI(callback: (Result<ROI>) -> Unit) = scoped { repo.getROI(roiDir).collect(callback) }

    fun saveBuffersFile(buffers: Buffers) = scoped { repo.saveBuffersFile(roiDir, buffers).collect() }
    fun loadBuffersFile(callback: (Result<Buffers>) -> Unit) = scoped { repo.loadBuffersFile(roiDir).collect(callback) }
    fun getBuffers(callback: (ApiResult<Buffers>) -> Unit) {
        buffersJob?.cancel()
        apiWithToken({ buffersJob = it }, repo.getBuffers(roi)) { result ->
            buffersJob = null
            callback(result)
        }
    }

    fun saveBuffer(buffer: Int, callback: (Result<Unit>) -> Unit) = scoped {
        roi = roi.copy(buffDist = buffer)
        repo.saveBuffer(roiDir, roi, buffer).collect(callback)
    }

    override fun parentDir() = roiDir
    override fun bounds() = roi.bounds()
    override fun tileDir(i: Int) = tileDirs()[i]

    override fun saveVisualizeURLsFile(urls: VisualizeURLs) = scoped { repo.saveVisualizeURLs(roiDir, urls).collect() }
    override fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit) = scoped { repo.loadVisualizeURLs(roiDir).collect(callback) }
    override fun getVisualizeURLs(callback: (ApiResult<VisualizeURLs>) -> Unit) {
        visualizeURLsJob?.cancel()
        apiWithToken({ visualizeURLsJob = it }, repo.getVisualizeURLs(roi)) { result ->
            visualizeURLsJob = null
            callback(result)
        }
    }

    fun saveExports(exports: ImageryExports) = scoped { repo.saveExports(roiDir, exports).collect() }
    fun loadExports(callback: (Result<ImageryExports>) -> Unit) = scoped { repo.loadExports(roiDir).collect(callback) }
    fun getExports(visualize: Boolean, callback: (ApiResult<ImageryExports>) -> Unit) {
        exportsJobs?.cancel()
        apiWithToken({ exportsJobs = it }, repo.getExports(roi.copy(visualize = visualize))) { result ->
            exportsJobs = null
            callback(result)
        }
    }

    fun saveResults(results: TasksResults) = scoped { repo.saveResults(roiDir, results).collect() }
    fun loadResults(callback: (Result<TasksResults>) -> Unit) = scoped { repo.loadResults(roiDir).collect(callback) }
    fun getResults(exports: ImageryExports, callback: (ApiResult<TasksResults>) -> Unit) {
        statusJob?.cancel()
        getTasksResults({ statusJob = it }, listOf(exports.chot.name, exports.clot.name, exports.hhot.name, exports.hlot.name)) { apiResult ->
            statusJob = null

            if (apiResult is ApiResult.Success) {
                scoped {
                    repo.deleteResults(roiDir).collect {
                        callback(apiResult)
                    }
                }
            } else {
                callback(apiResult)
            }
        }
    }

    fun getChotUri(path: String, callback: (Result<Uri>) -> Unit) {
        chotUriJob?.cancel()
        uriFromStorage({ chotUriJob = it }, path) { result ->
            chotUriJob = null
            callback(result)
        }
    }
    fun getClotUri(path: String, callback: (Result<Uri>) -> Unit) {
        clotUriJob?.cancel()
        uriFromStorage({ clotUriJob = it }, path) { result ->
            clotUriJob = null
            callback(result)
        }
    }
    fun getHhotUri(path: String, callback: (Result<Uri>) -> Unit) {
        hhotUriJob?.cancel()
        uriFromStorage({ hhotUriJob = it }, path) { result ->
            hhotUriJob = null
            callback(result)
        }
    }
    fun getHlotUri(path: String, callback: (Result<Uri>) -> Unit) {
        hlotUriJob?.cancel()
        uriFromStorage({ hlotUriJob = it }, path) { result ->
            hlotUriJob = null
            callback(result)
        }
    }
}