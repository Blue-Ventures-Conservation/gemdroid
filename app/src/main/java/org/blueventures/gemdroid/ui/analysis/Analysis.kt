package org.blueventures.gemdroid.ui.analysis

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.analysis.cra.CRA.Routes.cont_cra
import org.blueventures.gemdroid.ui.roi.Roi
import org.blueventures.gemdroid.ui.analysis.separability.Separability
import org.blueventures.gemdroid.ui.analysis.separability.Separability.Routes.separabilityDashboard

object Analysis {
    object Routes {
        const val dashboard = "dashboard"
        const val buffer = "buffer"
        const val visualize = "visualize"
        const val classification = "classification"
        const val classification_map = "class_map"
        const val country = "country"
        const val dynamics = "dynamics"
        const val dynamics_map = "dyn_map"

        fun dashboardNext(stage: Stage): String? {
            return when(stage) {
                Stage.BUFFER -> buffer
                Stage.VISUALIZE -> visualize
                Stage.CRAS -> cont_cra
                Stage.SEPARABILITY -> separabilityDashboard
                Stage.CLASSIFICATION -> classification
                Stage.COUNTRY -> country
                Stage.DYNAMICS -> dynamics
                else -> null
            }
        }
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun) {
        // Dashboard
        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, appBar, snack, next = { stage ->
                Routes.dashboardNext(stage)?.let { route ->
                    nav.popClear(route)
                }
            }, back = {
                nav.popClear(Roi.Routes.list)
            }, vis = {
                nav.popClear(Routes.visualize)
            }, sep = {
                nav.popClear(separabilityDashboard)
            }, clazz = {
                nav.popClear(Routes.classification)
            }, dyn = {
                nav.popClear(Routes.dynamics)
            })
        }

        // Buffer selection
        b.composable(Routes.buffer) {
            Buffer.Screen(viewModel, appBar, snack) {
                nav.popClear(Routes.dashboard)
            }
        }

        // Visualization
        b.composable(Routes.visualize) {
            Visualize.Screen(viewModel, appBar) {
                nav.popClear(Routes.dashboard)
            }
        }

        // CRAs
        CRA.screens(b, nav, viewModel.craViewModel, appBar, snack)

        // Spectral Separability
        Separability.screens(b, nav, viewModel.sepViewModel, appBar, snack)
    }
}