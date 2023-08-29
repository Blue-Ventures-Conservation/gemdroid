package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Maps

object CoarseRoi {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back, next) { nav ->
            Maps.Screen<URLs>(
                attemptGps = true,
                draw = Draw.Model(snack, nav::next, viewModel.drawPoly)
            )
        }
    }
}