package org.blueventures.gemdroid.ui.settings.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.versionString
import org.blueventures.gemdroid.model.settings.SettingsViewModel
import org.blueventures.gemdroid.model.settings.State
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object Settings {
    @Composable
    fun Screen(viewModel: SettingsViewModel, appBar: AppBar, snack: SnackFun) {
        appBar.Update(AppBarUpdate(stringResource(R.string.settings_label), true))
        val (state, setState) = remember { mutableStateOf<State?>(null) }

        val ctx = LocalContext.current
        when (state) {
            null -> {
                Progress()
                viewModel.getState(ctx, setState)
            }
            else -> {
                Col.MidPad(scroll = true) {
                    Info.Txt(stringResource(R.string.more_settings_coming_soon))
//                    SettingsItem(label = stringResource(R.string.landsat_only), description = stringResource(R.string.landsat_only_description)) {
//                        SettingsCheckbox(state.forceLandsat) { force ->
//                            viewModel.setForceLandsat(ctx, force)
//                        }
//                    }
                    Info.Txt("${stringResource(R.string.app_version)} $versionString")
                }
            }
        }
    }

    @Composable
    fun SettingsItem(label: String, description: String, interaction: @Composable () -> Unit) {
        Info.Block {
            Info.Row {
                Column {
                    Info.Txt(label)
                    Info.Txt(description, 14.sp)
                }
                interaction()
            }
        }
    }

    @Composable
    fun SettingsCheckbox(init: Boolean, onChange: (Boolean) -> Unit) {
        val (checked, setChecked) = remember { mutableStateOf(init) }
        Checkbox(checked, onCheckedChange = { check ->
            setChecked(check)
            onChange(check)
        })
    }
}