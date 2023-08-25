package org.blueventures.gemdroid.ui.analysis.dynamics

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.DrawOrShapefile
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.DrawSubRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.NameRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.ShapefileSubRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.SubRegionsOption
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.TargetClass
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.VisualizeShapefile
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Dynamics {
    object Routes {
        const val dynamics_target_class = "analysis_dynamics_target_class"
        const val dynamics_sub_regions_option = "analysis_dynamics_sub_regions_option"
        const val dynamics_sub_region_name = "analysis_dynamics_sub_region_name"
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
            }, {
                nav.navigate(Routes.dynamics_map)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.dynamics_sub_regions_option) {
            SubRegionsOption.Screen(viewModel, yes = {
                nav.navigate(Routes.dynamics_sub_region_name)
            }, no = {
                nav.navigate(Routes.dynamics_map)
            }, back = {
                nav.popClear(Routes.dynamics_target_class)
            })
        }

        b.composable(Routes.dynamics_sub_region_name) {
            NameRegion.Screen(viewModel, snack, next = {
                nav.navigate(Routes.dynamics_sub_regions_draw_or_shapefile)
            }) {
                viewModel.regionName = ""
                nav.popBackStack()
            }
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

        b.composable(Routes.dynamics_drawn_sub_region) {
            DrawSubRegion.Screen(viewModel, snack, next = {
                nav.popClear(Routes.dynamics_sub_regions_option)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.dynamics_shapefile_region) {
            ShapefileSubRegion.Screen(viewModel, snack, next = {
                nav.navigate(Routes.dynamics_visualize_shapefile_region)
            }) {
                nav.popBackStack()
            }
        }

        b.composable(Routes.dynamics_visualize_shapefile_region) {
            VisualizeShapefile.Screen(viewModel, snack, next = {
                nav.popClear(Routes.dynamics_sub_regions_option)
            }) {
               nav.popBackStack()
            }
        }
    }
}