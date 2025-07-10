package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.blueventures.gemdroid.ui.theme.BVDarkBlue
import org.blueventures.gemdroid.ui.theme.BVGreen
import org.blueventures.gemdroid.ui.theme.MildRed

object ChooseRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, downloads: Click, next: Click) {
        appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
        val regions = mutableListOf(viewModel.urls.stats)
        for (subRegion in viewModel.urls.subRegionStats) {
            regions.add(subRegion)
        }

        Col.Col(scroll = true) {
            Legend()
            Column {
                Text(stringResource(R.string.choose_which_region_to_analyze), fontSize = 20.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.size(16.dp))
                regions.forEach { region ->
                    var label = region.name
                    var space = 16.dp
                    if (region.name == viewModel.roi.name) {
                        label = stringResource(R.string.full_project_area)
                        space = 48.dp
                    }
                    DashboardButton(label) {
                        viewModel.analysisRegion = region
                        next()
                    }
                    Spacer(modifier = Modifier.size(space))
                }
            }
            Downloads(downloads)
        }
    }

    @Composable
    private fun Legend() {
        Info.Block {
            Info.Header(stringResource(R.string.legend))
            LegendRow(stringResource(R.string.loss), MildRed)
            LegendRow(stringResource(R.string.persistence), BVGreen)
            LegendRow(stringResource(R.string.gain), BVDarkBlue)
        }
    }

    @Composable
    private fun LegendRow(label: String, color: Color) {
        Info.Row {
            Info.Txt(label)
            Box(modifier = Modifier
                .background(color)
                .size(24.dp))
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