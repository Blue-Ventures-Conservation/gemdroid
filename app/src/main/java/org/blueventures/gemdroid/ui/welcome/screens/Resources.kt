package org.blueventures.gemdroid.ui.welcome.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.desktopUri
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.publicationUri
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.userManualUri
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object Resources {
    @Composable
    fun Screen(appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.introduction), true))

        val context = LocalContext.current

        Col.Col(scroll = true) {
            Info.Block {
                Info.Header(stringResource(R.string.gem_resources))
                Info.Space()
                Info.SubHeader(stringResource(R.string.tap_to_open), truncate = false)
            }
            Info.Space()
            Info.Block {
                Info.Row(horizontalArrangement = Arrangement.Center, enabled = true, click = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(userManualUri)))
                }) {
                    Info.Txt(stringResource(R.string.user_manual), verticalPadding = 8.dp, textAlign = TextAlign.Center)
                }
                Info.Space()
                Info.Row(horizontalArrangement = Arrangement.Center, enabled = true, click = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(publicationUri)))
                }) {
                    Info.Txt(stringResource(R.string.publication), verticalPadding = 8.dp, textAlign = TextAlign.Center)
                }
                Info.Space()
                Info.Row(horizontalArrangement = Arrangement.Center, enabled = true, click = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(desktopUri)))
                }) {
                    Info.Txt(stringResource(R.string.desktop_repo), verticalPadding = 8.dp, textAlign = TextAlign.Center)
                }
            }
            Info.Space()
            Butt.Next(click = next)
        }
    }
}