package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Capture
import org.blueventures.gemdroid.ui.common.maps.ClickContent
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MultiMapActionButtons
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Creation {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBar, snack: SnackFun, done: Click) {
        Visualize.Capture(viewModel.visualizer, appBar, stringResource(R.string.classification_reference_areas),
            true, Bounds.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), 16f, Maps.Storage.fromViewModel(viewModel),
            capture = Capture.Model(viewModel, snack, done)) {
            val createCRAsFirst = stringResource(R.string.please_create_some_cras_before_tapping_the_done_button)
            MultiMapActionButtons(ClickContent({
                viewModel.nextCapture()
            }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = stringResource(R.string.capture_the_current_area))
            }, ClickContent({
                viewModel.nextShape()
            }) {
                Icon(Icons.Filled.FormatShapes, contentDescription = stringResource(R.string.modify_polygon_shape))
            }, ClickContent({
                if (viewModel.capturedCollection.features.isNotEmpty()) {
                    viewModel.doneClick()
                } else {
                    snack(createCRAsFirst)
                }
            }) {
                Icon(Icons.Filled.DoneAll, contentDescription = stringResource(R.string.finished_creating_cras))
            })
        }
    }
}