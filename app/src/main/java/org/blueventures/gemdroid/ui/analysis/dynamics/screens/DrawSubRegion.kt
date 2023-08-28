package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Visualize

object DrawSubRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, next: Click, back: Click) {
        Visualize.Screen(stringResource(R.string.draw_sub_region), viewModel.visualizer, appBar, next = {
            viewModel.polygonDrawn()
            next()
        }, draw = viewModel.drawPoly)
        BackHandler(onBack = back)
    }
}