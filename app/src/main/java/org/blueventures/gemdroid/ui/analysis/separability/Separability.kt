package org.blueventures.gemdroid.ui.analysis.separability

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Separability {
    object Routes {
        const val timePeriod = "time_period"
        const val separabilityDashboard = "sep_dashboard"
        const val separation = "separation"
        const val scatter = "scatter"
        const val correlation = "correlation"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.timePeriod) {
            SelectTimePeriod.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.separabilityDashboard)
            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }

        b.composable(Routes.separabilityDashboard) {
            Dashboard.Screen(viewModel, appBar, separation = {
                nav.navigate(Routes.separation)
            }, scatter = {
                nav.navigate(Routes.scatter)
            }, correlation = {
                nav.navigate(Routes.correlation)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.separation) {
            Separation.Screen(viewModel) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.scatter) {

        }

        b.composable(Routes.correlation) {

        }
    }
}