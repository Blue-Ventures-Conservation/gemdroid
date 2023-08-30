package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Nav

object DrawOrShapefile {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun,  draw: Click, shapefile: Click, back: Click) {
        Nav.Wrap(back) { nav ->
            appBar(AppBarUpdate(viewModel.roi.name + " " + stringResource(R.string.dynamics)))
            Col.Dash(stringResource(R.string.draw_sub_region_or_shapefile)) {
                DashboardButton(stringResource(R.string.draw_a_sub_region)) {
                    nav.next()
                    draw()
                }
                DashboardButton(stringResource(R.string.use_a_shapefile)) {
                    nav.next()
                    shapefile()
                }
            }
        }
    }
}