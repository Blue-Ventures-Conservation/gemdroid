package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import java.io.File

class AnalysisViewModel(private val repo: AnalysisRepository = AnalysisRepository()): BaseViewModel() {
    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state

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

    fun saveBuffersFile(buffers: Buffers.Data) = scoped {
        repo.saveBuffersFile(_state.value.roiDir, buffers).collect()
    }

    fun getBuffersFile() = scoped {
        repo.getBuffersFile(_state.value.roiDir).collect { buffers ->
            buffers?.let {
                newState(_state.value.copy(buffers = it))
            } ?: run {
                newState(_state.value.copy(buffers = Buffers.empty()))
            }
        }
    }

    fun getBuffers(roi: ROI.Data, callback: () -> Unit = {}) = scoped {
        repo.getBuffers(roi).collect { result ->
            newState(_state.value.copy(buffersResult = result))
            callback()
        }
    }

    fun clearBuffer() {
        newState(_state.value.copy(roi = null, buffers = null, buffersResult = null))
    }

    fun saveBuffer(buffer: Int, callback: (Boolean) -> Unit) = scoped {
        repo.saveBuffer(_state.value.roiDir, buffer).collect {
            callback(it)
        }
    }

    fun setRoiDir(dir: File) = newState(AnalysisState(roiDir = dir))

    fun clear() = newState(AnalysisState())
    private fun newState(state: AnalysisState) { _state.value = state }
}

data class AnalysisState(
    val roiDir: File = File(""),
    val stage: Stage? = null,
    val roi: Result<ROI.Data>? = null,
    val buffers: Buffers.Data? = null,
    val buffersResult: ApiResult<Buffers.Data>? = null,
)