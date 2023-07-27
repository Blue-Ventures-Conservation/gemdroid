package org.blueventures.gemdroid.ui.analysis.dynamics

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Dynamics {
    object Routes {
        const val dynamics_map = "analysis_dynamics_map"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.dynamics_map) {
            // TODO
        }
    }
}