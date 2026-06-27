package org.blueventures.gemdroid.ui.welcome

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.settings.SettingsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.roi.Roi
import org.blueventures.gemdroid.ui.welcome.screens.Final
import org.blueventures.gemdroid.ui.welcome.screens.Landing
import org.blueventures.gemdroid.ui.welcome.screens.Resources

object Welcome {
    object Routes {
        const val prefix = "welcome_"
        const val landing = prefix + "landing"
        const val resources = prefix + "resources"
        const val final = prefix + "final"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, activity: Activity, viewModel: SettingsViewModel, appBar: AppBar) {
        var shown = false
        b.backHandler(Routes.landing, {
            if (shown) {
                nav.popBackStack()
            } else {
                activity.finish()
            }
        }) {
            val (welcomeShown, setWelcomeShown) = remember { mutableStateOf<Boolean?>(null) }
            when (welcomeShown) {
                null -> {
                    viewModel.getWelcomeShown(activity, setWelcomeShown)
                }
                else -> {
                    shown = welcomeShown
                    Landing.Screen(appBar) {
                        nav.navigate(Routes.resources)
                    }
                }
            }
        }

        b.backHandler(Routes.resources, nav::popBackStack) {
            Resources.Screen(appBar) {
                nav.navigate(Routes.final)
            }
        }

        b.backHandler(Routes.final, nav::popBackStack) {
            Final.Screen(appBar) {
                viewModel.setWelcomeShown(activity, true)
                nav.popBackStack(Roi.Routes.list, false)
            }
        }
    }
}