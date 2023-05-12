package org.blueventures.gemdroid.ui.analysis.separability

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.data.JSONMap
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.SnackFun

object Separability {
    object Routes {
        const val timePeriod = "time_period"
        const val separabilityDashboard = "sep_dashboard"
        const val separation = "separation"
        const val scatterChoices = "scatter_choices"
        const val scatterPlot = "scatter_plot"
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
                nav.navigate(Routes.scatterChoices)
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

        b.composable(Routes.scatterChoices) {
            ScatterChoices.Screen(viewModel = viewModel, {
                nav.popBackStack()
            }) {
                nav.navigate(Routes.scatterPlot)
            }
        }

        b.composable(Routes.scatterPlot) {
            ScatterPlot.Screen(viewModel) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.correlation) {

        }
    }

    fun craErrorHandler(code: Int?, message: String?): Pair<String?, Boolean> {
        return if (code == 400 && message?.contains("missing asset") == true) {
            Pair("CRA not found on the backend.", false)
        } else {
            Pair(null, true)
        }
    }

    @Composable
    fun <T> Layout(json: Map<String, Any>, back: Click, chart: @Composable (T?, List<String>, List<String>, (T?) -> Unit) -> Unit) {
        val (data, setData) = remember { mutableStateOf<T?>(null) }
        val bandsAndClasses = JSONMap.bandsAndClasses(json)

        if (bandsAndClasses == null) {
            Effect.Once {
                back()
            }
            return
        }

        val bands = bandsAndClasses.first
        val classes = bandsAndClasses.second
        Col.Between {
            chart(data, bands, classes, setData)
        }
    }
}