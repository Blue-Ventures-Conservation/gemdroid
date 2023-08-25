package org.blueventures.gemdroid.ui.analysis.dynamics

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object DrawOrShapefile {
    @Composable
    fun Screen(draw: Click, shapefile: Click, back: Click) {
        Col.Dash(stringResource(R.string.draw_sub_region_or_shapefile)) {
            DashboardButton(stringResource(R.string.draw_a_sub_region), draw)
            DashboardButton(stringResource(R.string.use_a_shapefile), shapefile)
        }

        BackHandler(onBack = back)
    }
}