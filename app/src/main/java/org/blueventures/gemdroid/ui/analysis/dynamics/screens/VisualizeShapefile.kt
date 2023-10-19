package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
                override fun polygons() = if (viewModel.shapefile.isEmpty()) emptyList() else listOf(viewModel.shapefile)
            })
        }
    }
}