package org.blueventures.gemdroid.ui.analysis.classification.separability

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, separation: Click, scatter: Click, correlation: Click, back: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        Col.Dash(stringResource(R.string.select_a_chart)) {
            DashboardButton(label = stringResource(R.string.band_separation), separation)
            DashboardButton(label = stringResource(R.string.band_scatter_plot), scatter)
            DashboardButton(label = stringResource(R.string.band_correlation), correlation)
        }

        BackHandler(onBack = back)
    }
}