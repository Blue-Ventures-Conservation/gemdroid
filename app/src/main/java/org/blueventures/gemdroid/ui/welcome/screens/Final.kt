package org.blueventures.gemdroid.ui.welcome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object Final {
    @Composable
    fun Screen(appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.introduction), true))
        Col.Col(top = 0.dp, scroll = true) {
            Info.SubHeader(stringResource(R.string.to_see_this_introduction_again), false)
            Info.Block {
                Info.Row(horizontalArrangement = Arrangement.Center) {
                    Info.Txt(stringResource(R.string.in_the_top_right_of_the_screen), textAlign = TextAlign.Center)
                    Icon(Icons.Filled.ArrowOutward, stringResource(R.string.arrow_to_the_top_right))
                }
                Info.Row(horizontalArrangement = Arrangement.Center) {
                    Info.Txt(stringResource(R.string.tap_on), textAlign = TextAlign.Center)
                    Icon(Icons.Filled.MoreVert, stringResource(R.string.expand_app_menu))
                }
                Col.Col {
                    Info.Txt(stringResource(R.string.to_expand_the_app_s_menu), textAlign = TextAlign.Center)
                    Info.Space(2)
                    Info.Txt(stringResource(R.string.then_tap_on_settings), textAlign = TextAlign.Center)
                    Info.Space(2)
                    Info.Txt(stringResource(R.string.from_the_settings_menu_tap_on), textAlign = TextAlign.Center)
                    Info.Txt(stringResource(R.string.show_introduction), textAlign = TextAlign.Center)
                }
            }
            Info.Space()
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Info.Txt(stringResource(R.string.it_s_time_to_create_your_first_project))
                Butt.Text(stringResource(R.string.go_to_my_projects), click = next)
            }
        }
    }
}