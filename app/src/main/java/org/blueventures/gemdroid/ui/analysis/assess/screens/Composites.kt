package org.blueventures.gemdroid.ui.analysis.assess.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
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

object Composites {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, confirm: Click) {
        Visualize.Screen(viewModel, appBar, stringResource(R.string.tap_the_arrows_to_continue),
            center = Bounds.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), storage = Maps.Storage.fromViewModel(viewModel)) {
            MapActionButton(confirm) { Icon(Icons.Filled.DoubleArrow, stringResource(R.string.tap_here_to_continue)) }
        }
    }
}