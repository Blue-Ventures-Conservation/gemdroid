package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object FalseColorDescription {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, downloads: Click) {
        appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.visualize_imagery_title))))
        Col.Col(scroll = true) {
            Spacer(modifier = Modifier.height(0.dp))
            Info.Txt(stringResource(R.string.false_color_channels))
            Info.Txt(stringResource(R.string.false_color_mangroves))
            Col.DashboardButton(stringResource(R.string.downloads), downloads)
        }
    }
}