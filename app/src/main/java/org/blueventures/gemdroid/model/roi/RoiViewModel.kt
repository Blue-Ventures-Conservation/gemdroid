package org.blueventures.gemdroid.model.roi

import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class RoiViewModel(private val repo: RoiRepository = RoiRepository()): BaseViewModel() {
    private val _state = MutableStateFlow(RoiState())
    val state: StateFlow<RoiState> = _state

    fun refreshRois(filesDir: File) = scoped {
        repo.getRois(filesDir).collect { files ->
            newState(RoiState(rois = files))
        }
    }

    fun clear() {
        newState(RoiState())
    }

    private fun newState(state: RoiState) {
        _state.value = state
    }
}

data class RoiState(
    val rois: List<File>? = null,
    val name: String? = null,
    val historicalYearStart: Int? = null,
    val historicalYearEnd: Int? = null,
    val contemporaryYearStart: Int? = null,
    val contemporaryYearEnd: Int? = null,
    val indices: List<String>? = null
)
