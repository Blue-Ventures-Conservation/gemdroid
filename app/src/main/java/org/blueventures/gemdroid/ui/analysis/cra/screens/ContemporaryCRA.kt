package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.cra.LocalOrRemoteCRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object ContemporaryCRA {
    @Composable
    fun Screen(viewModel: CRAViewModel, snack: SnackFun, next: Click) {
        Common.Screen(viewModel, stringResource(R.string.contemporary), snack, next, next, null, { cra ->
            viewModel.contemporaryCRA = cra
        }) { key ->
            viewModel.contemporaryCRA = LocalOrRemoteCRAFile(storageKey = key)
        }
    }
}