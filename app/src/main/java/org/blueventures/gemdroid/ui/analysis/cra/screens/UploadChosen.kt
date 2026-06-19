package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object UploadChosen {
    @Composable
    fun Screen(next: Click) {
        Col.Col(scroll = true) {
            Info.Txt(stringResource(R.string.you_will_import_cras_or_reuse))
            Info.Txt(stringResource(R.string.cras_format))
            Spacer(modifier = Modifier.height(32.dp))
            Butt.Next(click = next)
        }
    }
}