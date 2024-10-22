package org.blueventures.gemdroid.ui.welcome.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.settings.SettingsDatasource
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object Landing {
    @Composable
    fun Screen(appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.introduction), true))

        val context = LocalContext.current
        Col.Col(scroll = true) {
            Info.Header(stringResource(R.string.welcome_to_gem))
            Info.Txt(stringResource(R.string.tagline))
            Info.Block {
                Info.Row(enabled = true, click = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SettingsDatasource.bvUri)))
                }, horizontalArrangement = Arrangement.Center) {
                    Col.Col(arrange = Arrangement.SpaceAround) {
                        Image(painterResource(id = R.drawable.bv_logo), contentDescription = stringResource(R.string.bv_logo_context_description))
                        Info.Txt(stringResource(R.string.from_blue_ventures_conservation), fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            Butt.Next(click = next)
        }
    }
}