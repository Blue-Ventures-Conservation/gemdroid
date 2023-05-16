package org.blueventures.gemdroid.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.settings.SettingsViewModel

object Settings {
    @Composable
    fun Screen(viewModel: SettingsViewModel, back: () -> Unit) {
        BackHandler(onBack = back)
    }
}