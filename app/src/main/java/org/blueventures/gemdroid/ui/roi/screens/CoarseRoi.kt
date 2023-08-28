package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.data.Stale
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps

object CoarseRoi {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Maps.Screen<Stale>(
            snack = snack,
            back = back,
            next = next,
            attemptGps = true,
            draw = viewModel.drawPoly
        )
    }
}