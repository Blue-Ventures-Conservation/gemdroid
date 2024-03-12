package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object CoarsePolygonPurpose {
    @Composable
    fun Screen(appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
        Col.Col(scroll = true) {
            Spacer(modifier = Modifier.height(0.dp))
            Info.Txt(stringResource(R.string.you_will_create_coarse_roi))
            Info.Txt(stringResource(R.string.buffered_from_coastline))
            Info.Txt(stringResource(R.string.maybe_sub_regions_later))
            Butt.Next(click = next)
        }
    }
}