package org.blueventures.gemdroid.ui.analysis

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.roi.Roi

object Analysis {
    object Routes {
        const val dashboard = "dashboard"
        const val buffer = "buffer"
        const val visualize = "visualize"
        const val cont_cra = "cont_cra"
        const val hist_cra = "hist_cra"
        const val separabilityDashboard = "sep_dashboard"
        const val correlation = "corr"
        const val lsSeparation = "ls_sep"
        const val indicesSeparation = "indices_sep"
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

    fun screens(b: NavGraphBuilder, nav: NavHostController, roiModel: RoiViewModel, viewModel: AnalysisViewModel, setAppBarState: (AppBarUpdate) -> Unit, snackbar: (String) -> Unit) {
        // Dashboard
        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, setAppBarState, snackbar, nextClick = { stage ->
                Routes.dashboardNext(stage)?.let { route ->
                    nav.popClear(route)
                }
            }, backClick = {
                roiModel.clear()
                nav.popClear(Roi.Routes.list)
            }, visClick = {
                nav.popClear(Routes.visualize)
            }, sepClick = {
                nav.popClear(Routes.separabilityDashboard)
            }, classClick = {
                nav.popClear(Routes.classification)
            }, dynClick = {
                nav.popClear(Routes.dynamics)
            })
        }

        // Buffer selection
        b.composable(Routes.buffer) {
            Buffer.Screen(viewModel, setAppBarState, snackbar) {
                nav.popClear(Routes.dashboard)
            }
        }

        // Visualization
        b.composable(Routes.visualize) {
            Visualize.Screen(viewModel, setAppBarState) {
                nav.popClear(Routes.dashboard)
            }
        }

        // CRAs
        b.composable(Routes.cont_cra) {
            ContemporaryCRA.Screen(viewModel, setAppBarState, snackbar, {
                nav.navigate(Routes.hist_cra)
            }) {
                nav.popClear(Routes.dashboard)
            }
        }
        b.composable(Routes.hist_cra) {
            HistoricalCRA.Screen()
        }
    }
}