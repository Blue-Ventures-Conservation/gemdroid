package org.blueventures.gemdroid.ui.analysis.dynamics

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Details
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Downloads
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Map
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.TargetClass
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object Dynamics {
    object Routes {
        const val target_class = "analysis_dynamics_target_class"
        const val map = "analysis_dynamics_map"
        const val details = "analysis_dynamics_details"
        const val downloads = "analysis_dynamics_downloads"
        const val polygonsPrefix = "analysis_dynamics_"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: DynamicsViewModel, appBar: AppBar, snack: SnackFun) {
        val polysBack = Polygons.screens(b, nav, Routes.polygonsPrefix, Analysis.Routes.dashboard, Routes.target_class, viewModel, appBar, snack)

        b.composable(Routes.target_class) {
            TargetClass.Screen(viewModel, appBar, polysBack) {
                viewModel.saveSubRegionsFile()
                nav.navigate(Routes.map)
            }
        }

        b.composable(Routes.map) {
            Map.Screen(viewModel, appBar, nav::popBackStack) {
                nav.navigate(Routes.details)
            }
        }

        b.composable(Routes.details) {
            Details.Screen(viewModel, appBar, nav::popBackStack) {
                nav.navigate(Routes.downloads)
            }
        }

        b.composable(Routes.downloads) {
            Downloads.Screen(viewModel, appBar, nav::popBackStack)
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        return Classification.errHandler(ctx, code, message)
    }
}