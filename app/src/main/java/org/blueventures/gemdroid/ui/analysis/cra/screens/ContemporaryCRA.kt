package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun

object ContemporaryCRA {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBarFun, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back, next) { nav ->
            appBar(AppBarUpdate(stringResource(R.string.classification_reference_areas)))
            Common.Screen(viewModel, stringResource(R.string.contemporary), snack, nav::next, nav::back, null, { cra ->
                viewModel.contemporaryCRA = cra
            }) { key ->
                viewModel.contemporaryCRA = CRAFile(storageKey = key)
            }
        }
    }
}