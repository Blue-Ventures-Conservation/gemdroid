package org.blueventures.gemdroid.ui.analysis.classification.separability

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.Correlation
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.Dashboard
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.ScatterChoices
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.ScatterClasses
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.ScatterPlot
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.SelectTimePeriod
import org.blueventures.gemdroid.ui.analysis.classification.separability.screens.Separation
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun

object Separability {
    object Routes {
        const val prefix = "analysis_classification_separability_"
        const val timePeriod = prefix + "time_period"
        const val dashboard = prefix + "dash"
        const val separation = prefix + "separation"
        const val scatterClasses = prefix + "scatter_classes"
        const val scatterBands = prefix + "scatter_bands"
        const val scatterPlot = prefix + "scatter_plot"
        const val correlation = prefix + "correlation"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: SeparabilityViewModel, appBar: AppBar, snack: SnackFun) {
        b.composable(Routes.timePeriod) {
            SelectTimePeriod.Screen(viewModel, appBar, snack, nav::popBackStack) {
                nav.navigate(Routes.dashboard)
            }
        }

        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, appBar, nav::popBackStack, separation = {
                nav.navigate(Routes.separation)
            }, scatter = {
                nav.navigate(Routes.scatterClasses)
            }) {
                nav.navigate(Routes.correlation)
            }
        }

        b.composable(Routes.separation) {
            Separation.Screen(viewModel, appBar, nav::popBackStack)
        }

        b.composable(Routes.scatterClasses) {
            ScatterClasses.Screen(viewModel, appBar, snack, nav::popBackStack) {
                nav.navigate(Routes.scatterBands)
            }
        }

        b.composable(Routes.scatterBands) {
            ScatterChoices.Screen(viewModel, appBar, nav::popBackStack) {
                nav.navigate(Routes.scatterPlot)
            }
        }

        b.composable(Routes.scatterPlot) {
            ScatterPlot.Screen(viewModel, appBar, nav::popBackStack)
        }

        b.composable(Routes.correlation) {
            Correlation.Screen(viewModel, appBar, nav::popBackStack)
        }
    }
}