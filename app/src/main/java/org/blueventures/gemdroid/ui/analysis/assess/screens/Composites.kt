package org.blueventures.gemdroid.ui.analysis.assess.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
        Visualize.Screen(viewModel, appBar, viewModel.roi.appBarTitle(stringResource(R.string.do_your_composites_look_good)),
            center = Bounds.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), storage = Maps.Storage.fromViewModel(viewModel), floating = {
                MapActionButton({
                    viewModel.saveCompositesAssessedFile()
                    confirm()
                }) { Icon(Icons.Filled.Check, stringResource(R.string.tap_here_to_confirm_the_composites_look_good)) }
            }
        )
    }
}