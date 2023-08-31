package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun

object ShapefileSubRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        Nav.Wrap(back, next) { nav ->
            appBar(AppBarUpdate(viewModel.roi.appBar(stringResource(R.string.dynamics))))
            Col.Col {
                Shapefile.Screen(stringResource(R.string.select_a_sub_region_shapefile), { bg ->
                    viewModel.background(bg)
                }, viewModel::validateShapefile, { err ->
                    snack(err)
                }) { points ->
                    viewModel.shapefile = points
                    nav.next()
                }
            }
        }
    }
}