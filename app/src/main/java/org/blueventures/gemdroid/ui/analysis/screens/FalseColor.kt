package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Visualize

object FalseColor {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, back: Click, downloads: Click) {
        Nav.Wrap(back, downloads) { nav ->
            Visualize.Screen(viewModel, viewModel.roi.appBar(stringResource(R.string.visualize_imagery_title)), appBar, floatingContent = {
                MapActionButton(nav::next) { Icon(Icons.Filled.Download, stringResource(R.string.download_imagery)) }
            })
        }
    }
}