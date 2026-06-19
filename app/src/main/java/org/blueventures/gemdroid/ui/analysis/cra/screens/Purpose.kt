package org.blueventures.gemdroid.ui.analysis.cra.screens

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

object Purpose {
    @Composable
    fun Screen(appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.classification_reference_areas)))
        Col.Col(scroll = true) {
            Info.Txt(stringResource(R.string.you_will_import_cras))
            Info.Txt(stringResource(R.string.cras_simple_description))
            // TODO: update the string below, CRA sizes can be much smaller for Sentinel-2 plots,
            // TODO: and verify that this string works in other languages
            Info.Txt(stringResource(R.string.cras_format))
            // TODO: remove the string below
            Info.Txt(stringResource(R.string.we_will_add_feature))
            Spacer(modifier = Modifier.height(32.dp))
            Butt.Next(click = next)
        }
    }
}