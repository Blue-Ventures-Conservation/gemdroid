package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Satellite {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, imageryDescription: Click) {
        Visualize.Screen(viewModel, appBar, viewModel.roi.appBarTitle(stringResource(R.string.visualize_imagery_title)),
            center = Bounds.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), storage = Maps.Storage.fromViewModel(viewModel), floating = {
                MapActionButton(imageryDescription) { Icon(Icons.Filled.Info, stringResource(R.string.false_color_imagery_description)) }
            }
        )
    }
}