package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Roi.OverviewFromROI

object ReviewInputs {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, back: Click, polygon: Click) {
        Nav.Wrap(back) {
            val roi = viewModel.roi
            appBar.Update(AppBarUpdate(stringResource(R.string.review_inputs)))
            OverviewFromROI(roi, stringResource(R.string.overview), stringResource(R.string.coarse_boundary), polygon)
        }
    }
}