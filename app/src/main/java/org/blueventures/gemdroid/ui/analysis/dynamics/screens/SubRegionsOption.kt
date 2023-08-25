package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object SubRegionsOption {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, yes: Click, no: Click, back: Click) {
        var header = R.string.would_you_like_subregions
        var yesButton = R.string.yes_add_sub_regions
        var noButton = R.string.no_skip_sub_regions

        if (viewModel.subRegions.isNotEmpty()) {
            header = R.string.would_you_like_more_sub_regions
            yesButton = R.string.yes_add_another_sub_region
            noButton = R.string.no_skip_more_sub_regions
        }

        Col.Dash(stringResource(header)) {
            DashboardButton(stringResource(yesButton), yes)
            DashboardButton(stringResource(noButton)) {
                viewModel.saveSubRegionsFile()
                no()
            }
        }

        BackHandler(onBack = back)
    }
}