package org.blueventures.gemdroid.ui.settings

import android.app.Activity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.settings.SettingsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.settings.screens.Settings

object AppSettings {
    object Routes {
        const val settings = "app_settings"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, activity: Activity, viewModel: SettingsViewModel, appBar: AppBar, snack: SnackFun) {
        b.backHandler(Routes.settings, nav::popBackStack) {
            Settings.Screen(viewModel, appBar, snack)
        }
    }
}