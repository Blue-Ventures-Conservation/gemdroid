package org.blueventures.gemdroid.ui.analysis.dynamics

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object SubRegionsOption {
    @Composable
    fun Screen(yes: Click, no: Click, back: Click) {
        Col.Dash(stringResource(R.string.would_you_like_subregions)) {
            DashboardButton(stringResource(R.string.yes_add_sub_regions), yes)
            DashboardButton(stringResource(R.string.no_skip_sub_regions), no)
        }

        BackHandler(onBack = back)
    }
}