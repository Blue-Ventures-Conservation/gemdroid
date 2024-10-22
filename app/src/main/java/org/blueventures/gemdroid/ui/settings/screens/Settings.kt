package org.blueventures.gemdroid.ui.settings.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.versionString
import org.blueventures.gemdroid.model.settings.SettingsViewModel
import org.blueventures.gemdroid.model.settings.State
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Progress

object Settings {
    @Composable
    fun Screen(viewModel: SettingsViewModel, appBar: AppBar, welcome: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.settings_label), true))
        val (state, setState) = remember { mutableStateOf<State?>(null) }

        val context = LocalContext.current
        when (state) {
            null -> {
                Progress()
                viewModel.getState(context, setState)
            }
            else -> {
                Col.MidPad(scroll = true) {
                    Info.Block {
                        val (lsOnly, setLSOnly) = remember { mutableStateOf(state.forceLandsat) }
                        SettingsItem(label = stringResource(R.string.landsat_only), description = stringResource(R.string.landsat_only_description), {
                            setLSOnly(!lsOnly)
                        }) {
                            Checkbox(lsOnly, onCheckedChange = { check ->
                                setLSOnly(check)
                                viewModel.setForceLandsat(context, check)
                            })
                        }
                        Info.Space(3)
                        SettingsItem(label = "Show Introduction", "", welcome, Arrangement.SpaceAround) {}
                        Info.Space()
                        SettingsItem(label = "Contact Us", "", {
                            context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:") // Only email apps handle this.
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("gem@blueventures.org"))
                            })
                        }, Arrangement.SpaceAround) {}
                    }

                    Info.Txt("${stringResource(R.string.app_version)} $versionString")
                }
            }
        }
    }

    @Composable
    fun SettingsItem(label: String, description: String, click: Click = {}, horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween, interaction: @Composable () -> Unit) {
        Info.Block {
            Info.Row(enabled = true, click = click, horizontalArrangement = horizontalArrangement) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Info.Txt(label)
                    if (description.isNotBlank()) {
                        Info.Txt(description, 14.sp)
                    }
                }
                interaction()
            }
        }
    }
}