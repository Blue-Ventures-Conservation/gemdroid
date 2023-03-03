package org.blueventures.gemdroid.ui.analysis

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.blueventures.gemdroid.model.analysis.AnalysisState
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.CRAFile
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.InputStream

object HistoricalCRA {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, snack: SnackFun, next: Click, back: Click) {
        CRA.Screen(viewModel, snack, back) { vm, state, remoteCRAs, snk ->
            SelectHistoricalCRA(vm, state, remoteCRAs, snk, next)
        }
    }

    @Composable
    fun SelectHistoricalCRA(viewModel: AnalysisViewModel, state: State<AnalysisState>, remoteCRAs: List<String>, snack: SnackFun, next: Click) {
        val result = remember { mutableStateOf<List<Uri>?>(null) }
        val (validating, setValidating) = remember { mutableStateOf(false) }

        if (validating) {
            Progress()
            if (state.value.localHistoricalCRA == null) {
                val streams = arrayListOf<InputStream?>()
                val names = arrayListOf<String?>()
                result.value?.forEach { uri ->
                    streams.add(LocalContext.current.contentResolver.openInputStream(uri))
                    names.add(CRA.contentDisplayName(LocalContext.current, uri))
                }
                viewModel.validateLocalHistoricalCRA(state.value.roiDir, streams, names)
            } else {
                when {
                    state.value.localHistoricalCRA!!.isFailure -> {
                        LaunchedEffect(state.value.localHistoricalCRA) {
                            snack(state.value.localHistoricalCRA!!.exceptionOrNull()!!.message!!)
                            setValidating(false)
                            result.value = null
                            viewModel.clearLocalHistoricalCRA()
                        }
                    }
                    state.value.localHistoricalCRA!!.isSuccess -> {
                        LaunchedEffect(key1 = state.value.localHistoricalCRA) {
                            viewModel.setHistoricalCRA(state.value.localHistoricalCRA!!.getOrNull()!!)
                            setValidating(false)
                            result.value = null
                            viewModel.clearLocalHistoricalCRA()
                            next()
                        }
                    }
                }
            }
        } else {
            CRA.Selection(viewModel, "Historical", remoteCRAs, result, setValidating, next) {
                viewModel.setHistoricalCRA(CRAFile(storageKey = it))
            }
        }
    }
}