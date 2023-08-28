package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun

object ShapefileSubRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, snack: SnackFun, next: Click, back: Click) {
        Shapefile.Screen(stringResource(R.string.select_a_sub_region_shapefile), viewModel::validateShapefile, { err ->
            snack(err)
        }) { points ->
            viewModel.shapefile = points
            viewModel.regionName = ""
            next()
        }
        BackHandler(onBack = back)
    }
}