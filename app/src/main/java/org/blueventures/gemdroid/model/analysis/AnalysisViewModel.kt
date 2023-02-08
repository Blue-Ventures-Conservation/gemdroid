package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class AnalysisViewModel(private val repo: AnalysisRepository = AnalysisRepository()): BaseViewModel() {
    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state

    fun refreshStage() = scoped {
        repo.getStage(_state.value.roiDir).collect { stage ->
            newState(_state.value.copy(stage = stage))
        }
    }

    fun setRoiDir(dir: File) = newState(AnalysisState(roiDir = dir))

    fun clear() = newState(AnalysisState())
    private fun newState(state: AnalysisState) { _state.value = state }
}

data class AnalysisState(
    val stage: Stage? = null,
    val roiDir: File = File(""),
)