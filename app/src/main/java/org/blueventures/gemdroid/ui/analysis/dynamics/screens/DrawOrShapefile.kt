package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Nav

object DrawOrShapefile {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, draw: Click, shapefile: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
            Col.Dash(stringResource(R.string.draw_sub_region_or_shapefile)) {
                DashboardButton(stringResource(R.string.draw_a_sub_region), draw)
                DashboardButton(stringResource(R.string.use_a_shapefile), shapefile)
            }
        }
    }
}