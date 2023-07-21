package org.blueventures.gemdroid.ui.analysis.classification.separability

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun
import java.net.HttpURLConnection

object Separability {
    object Routes {
        const val timePeriod = "analysis_classification_separability_time_period"
        const val dashboard = "analysis_classification_separability_dash"
        const val separation = "analysis_classification_separability_separation"
        const val scatterChoices = "analysis_classification_separability_scatter_choices"
        const val scatterPlot = "analysis_classification_separability_scatter_plot"
        const val correlation = "analysis_classification_separability_correlation"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.timePeriod) {
            SelectTimePeriod.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.dashboard)
            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }

        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, appBar, separation = {
                nav.navigate(Routes.separation)
            }, scatter = {
                nav.navigate(Routes.scatterChoices)
            }, correlation = {
                nav.navigate(Routes.correlation)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.separation) {
            Separation.Screen(viewModel, appBar, nav::popBackStack)
        }

        b.composable(Routes.scatterChoices) {
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

    fun craErrorHandler(code: Int?, message: String?): Pair<String?, Boolean> {
        return if (code == HttpURLConnection.HTTP_BAD_REQUEST && message?.contains("missing asset") == true) {
            Pair("CRA not found on the backend.", false)
        } else {
            Pair(null, true)
        }
    }
}