package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Visualize

object FalseColor {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, back: Click) {
        Nav.Wrap(back) {
            Visualize.Screen(viewModel, viewModel.roi.appBar(stringResource(R.string.visualize_imagery_title)), appBar)
        }
    }
}