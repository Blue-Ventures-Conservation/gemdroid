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
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.InputStream

object ContemporaryCRA {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        appBar(AppBarUpdate(title = "Classification Reference Areas (CRAs)"))
        CRA.Screen(viewModel, snack, back) { vm, state, remoteCRAs, snk ->
            SelectContemporaryCRA(vm, state, remoteCRAs, snk, next)
        }
    }

    @Composable
    fun SelectContemporaryCRA(viewModel: AnalysisViewModel, state: State<AnalysisState>, remoteCRAs: List<String>, snack: SnackFun, next: Click) {
        val result = remember { mutableStateOf<List<Uri>?>(null) }
        val (validating, setValidating) = remember { mutableStateOf(false) }

        if (validating) {
            Progress()
            if (state.value.localContemporaryCRA == null) {
                val streams = arrayListOf<InputStream?>()
                val names = arrayListOf<String?>()
                result.value?.forEach { uri ->
                    streams.add(LocalContext.current.contentResolver.openInputStream(uri))
                    names.add(CRA.contentDisplayName(LocalContext.current, uri))
                }
                viewModel.validateLocalContemporaryCRA(state.value.roiDir, streams, names)
            } else {
                when {
                    state.value.localContemporaryCRA!!.isFailure -> {
                        LaunchedEffect(state.value.localContemporaryCRA) {
                            snack(state.value.localContemporaryCRA!!.exceptionOrNull()!!.message!!)
                            setValidating(false)
                            result.value = null
                            viewModel.clearLocalContemporaryCRA()
                        }
                    }
                    state.value.localContemporaryCRA!!.isSuccess -> {
                        LaunchedEffect(key1 = state.value.localContemporaryCRA) {
                            viewModel.setContemporaryCRA(state.value.localContemporaryCRA!!.getOrNull()!!)
                            setValidating(false)
                            result.value = null
                            viewModel.clearLocalContemporaryCRA()
                            next()
                        }
                    }
                }
            }
        } else {
            CRA.Selection(viewModel, "Contemporary", remoteCRAs, result, setValidating, next) {
                viewModel.setContemporaryCRA(CRAFile(storageKey = it))
            }
        }
    }
}