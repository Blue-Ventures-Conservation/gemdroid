package org.blueventures.gemdroid.ui.analysis.classification

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.classification.separability.Separability
import org.blueventures.gemdroid.ui.analysis.classification.separability.Separability.Routes.timePeriod
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Classification {
    object Routes {
        const val map = "analysis_classification_map"
        const val accuracy = "analysis_classification_accuracy"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: ClassificationViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.map) {
            Map.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.accuracy)
            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }

        b.composable(Routes.accuracy) {
            Map.Screen(viewModel, appBar, snack, {
                nav.navigate(timePeriod)
            }) {
                nav.popBackStack()
            }
        }

        Separability.screens(b, nav, viewModel.sepViewModel, appBar, snack)
    }
}