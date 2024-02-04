package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            Col.Col(scroll = true) {
                Column {
                    Text(stringResource(R.string.choose_which_region_to_analyze), fontSize = 20.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.size(16.dp))
                    regions.forEach { region ->
                        DashboardButton(region.name) {
                            viewModel.analysisRegion = region
                            next()
                        }
                        Spacer(modifier = Modifier.size(16.dp))
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
            DashboardButton(stringResource(R.string.imagery_downloads), downloads)
        }
    }
}