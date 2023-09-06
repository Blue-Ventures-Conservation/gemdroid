package org.blueventures.gemdroid.ui.analysis

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.analysis.classification.Classification.Routes.classification_map
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.analysis.cra.CRA.Routes.cont_cra
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics.Routes.dynamics_sub_regions_option
import org.blueventures.gemdroid.ui.analysis.screens.Buffer
import org.blueventures.gemdroid.ui.analysis.screens.Dashboard
import org.blueventures.gemdroid.ui.analysis.screens.FalseColor
import org.blueventures.gemdroid.ui.analysis.screens.ImageryDownloads
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.roi.Roi

object Analysis {
    object Routes {
        const val dashboard = "analysis_dashboard"
        const val buffer = "analysis_buffer"
        const val visualize = "analysis_visualize"
        const val imagery_downloads = "analysis_imagery_downloads"

        fun dashboardNext(stage: Stage): String? {
            return when(stage) {
                Stage.BUFFER -> buffer
                Stage.CRAS -> cont_cra
                else -> null
            }
        }
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun) {
        // Dashboard
        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, appBar, snack, next = {
                Routes.dashboardNext(viewModel.stage)?.let { route ->
                    nav.navigate(route)
                }
            }, back = {
                nav.popClear(Roi.Routes.list)
            }, vis = {
                nav.navigate(Routes.visualize)
            }, clazz = {
                nav.navigate(classification_map)
            }, dyn = {
                nav.navigate(dynamics_sub_regions_option)
            })
        }

        // Buffer selection
        b.composable(Routes.buffer) {
            Buffer.Screen(viewModel, appBar, snack) {
                nav.popBackStack()
            }
        }

        // Visualization
        b.composable(Routes.visualize) {
            FalseColor.Screen(viewModel, appBar, {
                nav.navigate(Routes.imagery_downloads)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.imagery_downloads) {
            ImageryDownloads.Screen(viewModel, appBar) {
                nav.popBackStack()
            }
        }

        // CRAs
        CRA.screens(b, nav, viewModel.craViewModel, appBar, snack)

        // Classification
        Classification.screens(b, nav, viewModel.classViewModel, appBar, snack)

        // Dynamics
        Dynamics.screens(b, nav, viewModel.dynamicsViewModel, appBar, snack)
    }
}