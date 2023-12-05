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
import org.blueventures.gemdroid.ui.analysis.cra.CRA.Routes.purpose
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.analysis.screens.Boundary
import org.blueventures.gemdroid.ui.analysis.screens.Buffer
import org.blueventures.gemdroid.ui.analysis.screens.Dashboard
import org.blueventures.gemdroid.ui.analysis.screens.Downloads
import org.blueventures.gemdroid.ui.analysis.screens.ExcludedRegions
import org.blueventures.gemdroid.ui.analysis.screens.FalseColorDescription
import org.blueventures.gemdroid.ui.analysis.screens.ReviewInputs
import org.blueventures.gemdroid.ui.analysis.screens.Satellite
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.roi.Roi

object Analysis {
    object Routes {
        const val prefix = "analysis_"
        const val dashboard = prefix + "dashboard"
        const val buffer = prefix + "buffer"
        const val visualize = prefix + "visualize"
        const val imagery_description = prefix + "imagery_description"
        const val imagery_downloads = prefix + "imagery_downloads"
        const val review_inputs = prefix + "review_inputs"
        const val boundary = prefix + "boundary"
        const val excluded = prefix + "excluded_regions"

        fun dashboardNext(stage: Stage): String? {
            return when(stage) {
                Stage.BUFFER -> buffer
                Stage.CRAS -> purpose
                else -> null
            }
        }
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun) {
        // Dashboard
        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, appBar, snack, next = {
                Routes.dashboardNext(viewModel.stage)?.let { route ->
                    nav.navigate(route)
                }
            }, back = {
                nav.popClear(Roi.Routes.list)
            }, falseColor = {
                nav.navigate(Routes.visualize)
            }, review = {
                nav.navigate(Routes.review_inputs)
            }, clazz = {
                nav.navigate(map)
            }, dyn = {
                nav.navigate(Dynamics.Routes.prefix + Polygons.Routes.polygons_option)
            })
        }

        // Buffer selection
        b.composable(Routes.buffer) {
            Buffer.Screen(viewModel, appBar, snack, nav::popBackStack)
        }

        // Visualization
        b.composable(Routes.visualize) {
            Satellite.Screen(viewModel, appBar, nav::popBackStack) {
                nav.navigate(Routes.imagery_description)
            }
        }

        b.composable(Routes.imagery_description) {
            FalseColorDescription.Screen(appBar, nav::popBackStack) {
                nav.navigate(Routes.imagery_downloads)
            }
        }

        b.composable(Routes.imagery_downloads) {
            Downloads.Screen(viewModel, appBar, nav::popBackStack)
        }

        b.composable(Routes.review_inputs) {
            ReviewInputs.Screen(viewModel, appBar, nav::popBackStack) {
                nav.navigate(Routes.boundary)
            }
        }

        b.composable(Routes.boundary) {
            Boundary.Screen(viewModel, appBar, nav::popBackStack) {
                nav.navigate(Routes.excluded)
            }
        }

        b.composable(Routes.excluded) {
            ExcludedRegions.Screen(viewModel, appBar, nav::popBackStack)
        }

        // CRAs
        CRA.screens(b, nav, viewModel.craViewModel, appBar, snack)

        // Classification
        Classification.screens(b, nav, viewModel.classViewModel, appBar, snack)

        // Dynamics
        Dynamics.screens(b, nav, viewModel.dynamicsViewModel, appBar, snack)
    }
}