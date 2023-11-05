package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object VisualizeShapefile {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, next: Click) {
        Nav.Wrap(back) {
            Visualize.Screen(viewModel.visualizer, stringResource(R.string.visualize_sub_region_shp), appBar, floatingContent = {
                MapActionButton({
                    viewModel.shapefileLooksGood()
                    next()
                }) { Icon(Icons.Filled.Check, stringResource(R.string.shp_looks_good)) }}, poly = object : Poly.Model() {
                override val menuTitle = stringResource(R.string.shapefile)
                override val touchEnabled = true
                override val labels = listOf(viewModel.regionName)
                override fun <T> markerWork(work: () -> T, callback: (T) -> Unit) = viewModel.background(work, callback)

                override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) {
                    if (viewModel.shapefile.isEmpty()) {
                        callback(emptyList())
                    } else {
                        callback(listOf(viewModel.shapefile))
                    }
                }
            })
        }
    }
}