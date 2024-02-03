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
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav

object ChooseRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, downloads: Click, next: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
            val regions = mutableListOf(viewModel.urls.stats)
            for (subRegion in viewModel.urls.subRegionStats) {
                regions.add(subRegion)
            }

            Col.Dash(stringResource(R.string.choose_which_region_to_analyze)) {
                regions.forEach { region ->
                    DashboardButton(region.name) {
                        viewModel.analysisRegion = region
                        next()
                    }
                }
                Downloads(downloads)
            }
        }
    }

    @Composable
    private fun Downloads(downloads: Click) {
        Info.Block {
            Info.BlueLine()
            Info.Space()
            DashboardButton(stringResource(R.string.downloads), downloads)
        }
    }
}