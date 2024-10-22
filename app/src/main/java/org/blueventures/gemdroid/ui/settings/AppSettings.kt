package org.blueventures.gemdroid.ui.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.settings.SettingsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.settings.screens.Settings
import org.blueventures.gemdroid.ui.welcome.Welcome

object AppSettings {
    object Routes {
        const val settings = "app_settings"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: SettingsViewModel, appBar: AppBar) {
        b.backHandler(Routes.settings, nav::popBackStack) {
            Settings.Screen(viewModel, appBar) {
                nav.navigate(Welcome.Routes.landing)
            }
        }
    }
}