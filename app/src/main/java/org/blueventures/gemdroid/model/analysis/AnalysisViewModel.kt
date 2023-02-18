package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import java.io.File

class AnalysisViewModel(private val repo: AnalysisRepository = AnalysisRepository()): BaseViewModel() {
    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state

    var buffersJob: Job? = null
    var visualizeURLsJob: Job? = null

    fun refreshStage() = scoped {
        repo.getStage(_state.value.roiDir).collect { stage ->
            newState(_state.value.copy(stage = stage))
        }
    }

    fun getROI() = scoped {
        repo.getROI(_state.value.roiDir).collect { roi ->
            roi?.let {
                newState(_state.value.copy(roi = Result.success(it)))
            } ?: run {
                newState(_state.value.copy(roi = Result.failure(Throwable("Could not read filesystem state!"))))
            }
        }
    }

    fun saveBuffersFile(buffers: Buffers) = scoped {
        repo.saveBuffersFile(_state.value.roiDir, buffers).collect()
    }

    fun loadBuffersFile() = scoped {
        repo.loadBuffersFile(_state.value.roiDir).collect { buffers ->
            buffers?.let {
                newState(_state.value.copy(buffers = it))
            } ?: run {
                newState(_state.value.copy(buffers = Buffers.empty()))
            }
        }
    }

    fun getBuffers(roi: ROI, callback: () -> Unit = {}) {
        if (buffersJob != null) {
            return
        }

        buffersJob = scoped {
            repo.getBuffers(roi).collect { result ->
                newState(_state.value.copy(buffersResult = result))
                buffersJob = null
                callback()
            }
        }
    }

    fun saveBuffer(buffer: Int, callback: (Boolean) -> Unit) = scoped {
        val roiCpy = _state.value.roi!!.getOrNull()!!.copy(buffDist = buffer)
        repo.saveBuffer(_state.value.roiDir, roiCpy, buffer).collect {
            newState(_state.value.copy(roi = Result.success(roiCpy)))
            callback(it)
        }
    }

    fun saveVisualizeURLs(urls: VisualizeURLs) = scoped {
        repo.saveVisualizeURLs(_state.value.roiDir, urls).collect()
    }

    fun loadVisualizeURLs() = scoped {
        repo.loadVisualizeURLs(_state.value.roiDir).collect { buffers ->
            buffers?.let {
                newState(_state.value.copy(visualizeURLs = it))
            } ?: run {
                newState(_state.value.copy(visualizeURLs = VisualizeURLs.empty()))
            }
        }
    }

    fun getVisualizeURLs(roi: ROI, callback: () -> Unit = {}) {
        if (visualizeURLsJob != null) {
            return
        }

        visualizeURLsJob = scoped {
            repo.getVisualizeURLs(roi).collect { result ->
                newState(_state.value.copy(visualizeURLsResult = result))
                visualizeURLsJob = null
                callback()
            }
        }
    }

    fun chotTileDir(): File = repo.chotTileDir(_state.value.roiDir)
    fun clotTileDir(): File = repo.clotTileDir(_state.value.roiDir)
    fun hhotTileDir(): File = repo.hhotTileDir(_state.value.roiDir)
    fun hlotTileDir(): File = repo.hlotTileDir(_state.value.roiDir)

    fun setRoiDir(dir: File) = newState(AnalysisState(roiDir = dir))

    fun clearVisualizeURLs() = scoped {
        newState(_state.value.copy(visualizeURLs = VisualizeURLs.empty(), visualizeURLsResult = null))
    }
    fun clearBuffers() = newState(_state.value.copy(buffers = null, buffersResult = null))
    fun clearStage() = newState(_state.value.copy(stage = null))
    fun clear() = newState(AnalysisState())
    private fun newState(state: AnalysisState) { _state.value = state }
}

data class AnalysisState(
    val roiDir: File = File(""),
    val stage: Stage? = null,
    val roi: Result<ROI>? = null,
    val buffers: Buffers? = null,
    val buffersResult: ApiResult<Buffers>? = null,
    val visualizeURLs: VisualizeURLs? = null,
    val visualizeURLsResult: ApiResult<VisualizeURLs>? = null
)