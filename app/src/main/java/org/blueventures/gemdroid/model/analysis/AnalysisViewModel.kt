package org.blueventures.gemdroid.model.analysis

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.analysis.Buffers
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.maps.Visualize
import java.io.File

class AnalysisViewModel(
    private val repo: AnalysisRepository = AnalysisRepository()
): Visualize.Visualizer, ApiViewModel(repo) {
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

    var stage: Stage = Stage.BUFFER

    fun init(activity: ComponentActivity) {
        craViewModel = activity.viewModels<CRAViewModel>().value
        classViewModel = activity.viewModels<ClassificationViewModel>().value
        classViewModel.init(activity, craViewModel)
        dynamicsViewModel = activity.viewModels<DynamicsViewModel>().value
        dynamicsViewModel.init(craViewModel, this)
    }

    var buffersJob: Job? = null
    var visualizeURLsJob: Job? = null

    fun tileDirs() = listOf(repo.chotTileDir(roiDir), repo.clotTileDir(roiDir), repo.hhotTileDir(roiDir), repo.hlotTileDir(roiDir))

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
}