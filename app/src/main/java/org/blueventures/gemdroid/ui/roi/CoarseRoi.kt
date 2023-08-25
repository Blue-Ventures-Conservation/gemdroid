package org.blueventures.gemdroid.ui.roi

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.SnackFun

object CoarseRoi {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Maps.Screen<Unit>(
            snack = snack,
            back = back,
            next = next,
            attemptGps = true,
            draw = object : Draw.Model() {
                override fun polygonOptions() = viewModel.polygonOpts()
                override fun addPoint(point: LatLng, callback: () -> Unit) = viewModel.addPoint(point, callback)
                override fun validatePolygon() = viewModel.validatePolygon()
                override fun polygonIterate(mapf: (LatLng) -> Unit) = viewModel.polygon.points.iterator().forEach(mapf)
                override fun maxSquareKms(): String = viewModel.squareKms(RoiViewModel.maxROIArea)
                override fun polygonSquareKms() = viewModel.polygonSquareKms()
                override fun points() = viewModel.polygon.points
                override fun clearPoints() = viewModel.clearPoints()
            }
        )
    }
}