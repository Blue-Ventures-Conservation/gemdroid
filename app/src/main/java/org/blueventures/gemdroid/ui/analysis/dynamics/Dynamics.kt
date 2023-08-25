package org.blueventures.gemdroid.ui.analysis.dynamics

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Dynamics {
    object Routes {
        const val dynamics_target_class = "analysis_dynamics_target_class"
        const val dynamics_sub_regions_option = "analysis_dynamics_sub_regions_option"
        const val dynamics_sub_regions_draw_or_shapefile = "analysis_dynamics_sub_regions_draw_or_shapefile"
        const val dynamics_drawn_sub_region = "analysis_dynamics_drawn_sub_region"
        const val dynamics_shapefile_region = "analysis_dynamics_shapefile_region"
        const val dynamics_visualize_shapefile_region = "analysis_dynamics_visualize_shapefile_region"
        const val dynamics_map = "analysis_dynamics_map"
        const val dynamics_details = "analysis_dynamics_details"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.dynamics_target_class) {
            TargetClass.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.dynamics_sub_regions_option)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.dynamics_sub_regions_option) {
            SubRegionsOption.Screen(yes = {
                nav.navigate(Routes.dynamics_sub_regions_draw_or_shapefile)
            }, no = {
                nav.navigate(Routes.dynamics_map)
            }, back = {
                nav.popBackStack()
            })
        }

        b.composable(Routes.dynamics_sub_regions_draw_or_shapefile) {
            DrawOrShapefile.Screen(draw = {
                nav.navigate(Routes.dynamics_drawn_sub_region)
            }, shapefile = {
                nav.navigate(Routes.dynamics_shapefile_region)
            }) {
                nav.popBackStack()
            }
        }
    }
}