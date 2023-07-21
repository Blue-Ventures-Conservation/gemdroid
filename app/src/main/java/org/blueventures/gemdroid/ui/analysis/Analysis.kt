package org.blueventures.gemdroid.ui.analysis

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.analysis.classification.Classification.Routes.map
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.analysis.cra.CRA.Routes.cont_cra
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.roi.Roi

object Analysis {
    object Routes {
        const val dashboard = "analysis_dashboard"
        const val buffer = "analysis_buffer"
        const val visualize = "analysis_visualize"
        const val dynamics = "analysis_dynamics"

        fun dashboardNext(stage: Stage): String? {
            return when(stage) {
                Stage.BUFFER -> buffer
                Stage.VISUALIZE -> visualize
                Stage.CRAS -> cont_cra
                Stage.CLASSIFICATION -> map
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
            }, clazz = {
                nav.popClear(map)
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

        // Classification
        Classification.screens(b, nav, viewModel.classViewModel, appBar, snack)
    }
}