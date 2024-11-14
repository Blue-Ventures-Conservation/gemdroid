package org.blueventures.gemdroid.ui.analysis.assess.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.analysis.screens.Dashboard
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object Description {
    @Composable
    fun Screen(appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.assess_imagery)))
        Col.Col(scroll = true) {
            Space()
            Info.Txt(stringResource(R.string.you_will_assess))
            Space()
            Info.Txt(stringResource(R.string.fit_for_purpose_if))
            Space()
            Info.Txt(stringResource(R.string.difference_in_tide_levels))
            Space()
            Info.Txt(stringResource(R.string.exposed_tidal_flats))
            Space()
            Info.Txt(stringResource(R.string.the_coastline_may_have_shifted_inland))
            Space()
            Info.Txt(stringResource(R.string.exposed_areas_of_mudflats_in_between_mangroves))
            Space()
            Info.Txt(stringResource(R.string.water_in_river_channels))
            Space()
            Info.Txt(stringResource(R.string.there_are_no_missing_pixels))
            Space()
            Butt.Next(click = next)
        }
    }

    @Composable
    fun Space() = Spacer(modifier = Modifier.height(32.dp))
}