package org.blueventures.gemdroid.ui.analysis

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.CRAFile
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object HistoricalCRA {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, snack: SnackFun, next: Click, back: Click) {
        CRA.Screen(viewModel, "Historical", snack, next, back, viewModel.contemporaryCRA.key(), { cra ->
            viewModel.historicalCRA = cra
        }) { key ->
            viewModel.historicalCRA = CRAFile(storageKey = key)
        }
    }
}