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
        const val separabilityDashboard = "sep_dashboard"
        const val correlation = "corr"
        const val lsSeparation = "ls_sep"
        const val indicesSeparation = "indices_sep"

        // these need to be lowercase and spaces need to be removed before submitting to backend
        const val black = "Black"
        const val blue = "Blue"
        const val lightBlue = "Light Blue"
        const val brown = "Brown"
        const val cyan = "Cyan"
        const val green = "Green"
        const val lightGreen = "Light Green"
        const val grey = "Grey"
        const val lightGrey = "Light Grey"
        const val orange = "Orange"
        const val pink = "Pink"
        const val purple = "Purple"
        const val red = "Red"
        const val white = "White"
        const val yellow = "Yellow"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.separabilityDashboard) {
            Dashboard.Screen(viewModel, appBar, snack, {

            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }
    }
}