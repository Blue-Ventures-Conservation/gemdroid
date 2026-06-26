package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Capture
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Creation {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBar, snack: SnackFun, done: Click) {
        Visualize.Capture(viewModel.visualizer, appBar, stringResource(R.string.classification_reference_areas),
            true, PolygonUtils.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), 16f,
            Maps.Storage.fromViewModel(viewModel), capture = Capture.Model(viewModel, viewModel.roi.useS2(), snack, done))
    }
}