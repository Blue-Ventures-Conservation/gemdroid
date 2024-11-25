package org.blueventures.gemdroid.ui.analysis

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.assess.Assess
import org.blueventures.gemdroid.ui.analysis.assess.Assess.Routes.assess_description
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.analysis.classification.Classification.Routes.map
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.analysis.cra.CRA.Routes.purpose
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.analysis.screens.Boundary
import org.blueventures.gemdroid.ui.analysis.screens.Dashboard
import org.blueventures.gemdroid.ui.analysis.screens.Downloads
import org.blueventures.gemdroid.ui.analysis.screens.FalseColorDescription
import org.blueventures.gemdroid.ui.analysis.screens.ReviewInputs
import org.blueventures.gemdroid.ui.analysis.screens.Satellite
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.roi.Roi

object Analysis {
    object Routes {
        const val prefix = "analysis_"
        const val dashboard = prefix + "dashboard"
        const val visualize = prefix + "visualize"
        const val imagery_description = prefix + "imagery_description"
        const val imagery_downloads = prefix + "imagery_downloads"
        const val review_inputs = prefix + "review_inputs"
        const val boundary = prefix + "boundary"

        fun dashboardNext(stage: Stage): String? {
            return when(stage) {
                Stage.COMPOSITES -> assess_description
                Stage.CRAS -> purpose
                Stage.CLASSIFICATION -> map
                else -> null
            }
        }
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, roiViewModel: RoiViewModel, appBar: AppBar, snack: SnackFun) {
        // Dashboard
        b.backHandler(Routes.dashboard, {
            nav.popClear(Roi.Routes.list)
        }) { back ->
            roiViewModel.isAssessmentEditor = false
            Dashboard.Screen(viewModel, appBar, snack, back, next = {
                Routes.dashboardNext(viewModel.stage)?.let { route ->
                    nav.navigate(route)
                }
            }, falseColor = {
                nav.navigate(Routes.visualize)
            }, review = {
                nav.navigate(Routes.review_inputs)
            }, clazz = {
                nav.navigate(map)
            }, dyn = {
                nav.navigate(Dynamics.Routes.prefix + Polygons.Routes.option)
            })
        }

        // Visualization
        b.backHandler(Routes.visualize, nav::popBackStack) {
            Satellite.Screen(viewModel, appBar) {
                nav.navigate(Routes.imagery_description)
            }
        }

        b.backHandler(Routes.imagery_description, nav::popBackStack) {
            FalseColorDescription.Screen(viewModel, appBar) {
                nav.navigate(Routes.imagery_downloads)
            }
        }

        b.backHandler(Routes.imagery_downloads, nav::popBackStack) { back ->
            Downloads.Screen(viewModel, appBar, back)
        }

        b.backHandler(Routes.review_inputs, nav::popBackStack) {
            ReviewInputs.Screen(viewModel, appBar) {
                nav.navigate(Routes.boundary)
            }
        }

        b.backHandler(Routes.boundary, nav::popBackStack) {
            Boundary.Screen(viewModel, appBar)
        }

        // Composite Assessment
        Assess.screens(b, nav, viewModel, roiViewModel, appBar, snack)

        // CRAs
        CRA.screens(b, nav, viewModel.craViewModel, appBar, snack)

        // Classification
        Classification.screens(b, nav, viewModel.classViewModel, appBar, snack)

        // Dynamics
        Dynamics.screens(b, nav, viewModel.dynamicsViewModel, appBar, snack)
    }
}