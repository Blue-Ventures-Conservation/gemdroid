package org.blueventures.gemdroid.ui.analysis.cra

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object HistoricalCRA {
    @Composable
    fun Screen(viewModel: CRAViewModel, snack: SnackFun, next: Click, back: Click) {
        CRA.Screen(viewModel, stringResource(R.string.historical), snack, next, back, viewModel.contemporaryCRA.key(), { cra ->
            viewModel.historicalCRA = cra
        }) { key ->
            viewModel.historicalCRA = CRAFile(storageKey = key)
        }
    }
}