package org.blueventures.gemdroid.ui.analysis.dynamics

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Details
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Downloads
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.DrawOrShapefile
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.DrawSubRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Map
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.NameRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.ShapefileSubRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.SubRegionsOption
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.SubRegionsOverview
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.TargetClass
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.VisualizeShapefile
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Dynamics {
    object Routes {
        const val target_class = "analysis_dynamics_target_class"
        const val sub_regions_option = "analysis_dynamics_sub_regions_option"
        const val sub_region_name = "analysis_dynamics_sub_region_name"
        const val sub_regions_draw_or_shapefile = "analysis_dynamics_sub_regions_draw_or_shapefile"
        const val drawn_sub_region = "analysis_dynamics_drawn_sub_region"
        const val shapefile_region = "analysis_dynamics_shapefile_region"
        const val visualize_shapefile_region = "analysis_dynamics_visualize_shapefile_region"
        const val sub_regions_overview = "analysis_dynamics_sub_regions_overview"
        const val map = "analysis_dynamics_map"
        const val details = "analysis_dynamics_details"
        const val downloads = "analysis_dynamics_downloads"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.sub_regions_option) {
            SubRegionsOption.Screen(viewModel, appBar, snack, back = {
                nav.popClear(Analysis.Routes.dashboard)
            }, skip = {
                nav.popClear(Routes.target_class)
            }, yes = {
                nav.navigate(Routes.sub_region_name)
            }, no = {
                var navRoute = { nav.popClear(Routes.target_class) }
                if (viewModel.subRegions.isEmpty()) {
                    viewModel.saveSubRegionsFile()
                } else if (!viewModel.subRegionsLoaded && viewModel.subRegions.size > 1) {
                    navRoute = { nav.popClear(Routes.sub_regions_overview) }
                }
                navRoute()
            })
        }

        b.composable(Routes.sub_region_name) {
            NameRegion.Screen(viewModel, appBar, snack, nav::popBackStack) {
                nav.navigate(Routes.sub_regions_draw_or_shapefile)
            }
        }

        b.composable(Routes.sub_regions_draw_or_shapefile) {
            DrawOrShapefile.Screen(viewModel, appBar, nav::popBackStack, draw = {
                nav.navigate(Routes.drawn_sub_region)
            }) {
                nav.navigate(Routes.shapefile_region)
            }
        }

        b.composable(Routes.drawn_sub_region) {
            DrawSubRegion.Screen(viewModel, appBar, snack, {
                viewModel.drawPoly.clearAll()
                nav.popBackStack()
            }) {
                nav.popClear(Routes.sub_regions_option)
            }
        }

        b.composable(Routes.shapefile_region) {
            ShapefileSubRegion.Screen(viewModel, appBar, snack, nav::popBackStack){
                nav.navigate(Routes.visualize_shapefile_region)
            }
        }

        b.composable(Routes.visualize_shapefile_region) {
            VisualizeShapefile.Screen(viewModel, appBar, nav::popBackStack) {
                nav.popClear(Routes.sub_regions_option)
            }
        }

        b.composable(Routes.sub_regions_overview) {
            SubRegionsOverview.Screen(viewModel, appBar, {
                viewModel.subRegions.clear()
                nav.popClear(Analysis.Routes.dashboard)
            }, {
                viewModel.subRegions.clear()
                nav.popClear(Routes.sub_regions_option)
            }) {
                viewModel.saveSubRegionsFile()
                nav.popClear(Routes.target_class)
            }
        }

        b.composable(Routes.target_class) {
            TargetClass.Screen(viewModel, appBar, {
                nav.popClear(Analysis.Routes.dashboard)
            }) {
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