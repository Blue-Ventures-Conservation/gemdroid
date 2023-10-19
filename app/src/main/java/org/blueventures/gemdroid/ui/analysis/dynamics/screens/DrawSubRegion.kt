package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Visualize

object DrawSubRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            Visualize.Screen(viewModel.visualizer, stringResource(R.string.draw_sub_region), appBar, draw = Draw.Model(snack, next = {
                viewModel.polygonDrawn()
                next()
            }, viewModel.drawPoly))
        }
    }
}