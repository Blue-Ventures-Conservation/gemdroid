package org.blueventures.gemdroid.ui.analysis.classification.separability

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
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
import org.blueventures.gemdroid.ui.common.backHandler

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
        b.backHandler(Routes.timePeriod, nav::popBackStack) { back ->
            SelectTimePeriod.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.dashboard)
            }
        }

        b.backHandler(Routes.dashboard, nav::popBackStack) { back ->
            Dashboard.Screen(viewModel, appBar, back, separation = {
                nav.navigate(Routes.separation)
            }, scatter = {
                nav.navigate(Routes.scatterClasses)
            }) {
                nav.navigate(Routes.correlation)
            }
        }

        b.backHandler(Routes.separation, nav::popBackStack) { back ->
            Separation.Screen(viewModel, appBar, back)
        }

        b.backHandler(Routes.scatterClasses, nav::popBackStack) { back ->
            ScatterClasses.Screen(viewModel, appBar, snack, back) {
                nav.navigate(Routes.scatterBands)
            }
        }

        b.backHandler(Routes.scatterBands, nav::popBackStack) { back ->
            ScatterChoices.Screen(viewModel, appBar, back) {
                nav.navigate(Routes.scatterPlot)
            }
        }

        b.backHandler(Routes.scatterPlot, nav::popBackStack) { back ->
            ScatterPlot.Screen(viewModel, appBar, back)
        }

        b.backHandler(Routes.correlation, nav::popBackStack) { back ->
            Correlation.Screen(viewModel, appBar, back)
        }
    }
}