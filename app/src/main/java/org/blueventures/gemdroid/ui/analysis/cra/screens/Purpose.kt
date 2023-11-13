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
import org.blueventures.gemdroid.ui.common.Nav

object Purpose {
    @Composable
    fun Screen(appBar: AppBar, back: Click, next: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
            Col.Col(scroll = true) {
                Info.Txt(stringResource(R.string.you_will_import_cras))
                Info.Txt(stringResource(R.string.cras_simple_description))
                Info.Txt(stringResource(R.string.cras_format))
                Info.Txt(stringResource(R.string.we_will_add_feature))
                Spacer(modifier = Modifier.height(32.dp))
                Butt.Next(click = next)
            }
        }
    }
}