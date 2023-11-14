package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Maps

object CoarsePolygon {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            Maps.NoLayers(
                appBar = appBar,
                title = stringResource(R.string.create_coarse_roi),
                attemptGps = true,
                storage = Maps.Storage.fromViewModel(viewModel),
                draw = Draw.Model(viewModel.roiDrawer, snack, next)
            )
        }
    }
}