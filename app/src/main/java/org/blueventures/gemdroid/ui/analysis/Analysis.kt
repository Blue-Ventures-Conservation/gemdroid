package org.blueventures.gemdroid.ui.analysis

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.HistoricalChoice
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.roi.Roi

object Analysis {
    object Routes {
        const val dashboard = "dashboard"
        const val buffer = "buffer"
        const val visualize = "visualize"
        const val cont_cra = "cont_cra"
        const val hist_choice = "hist_choice"
        const val hist_cra = "hist_cra"
        const val cra_fields = "fields"
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

    fun screens(b: NavGraphBuilder, nav: NavHostController, roiModel: RoiViewModel, viewModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun) {
        // Dashboard
        b.composable(Routes.dashboard) {
            Dashboard.Screen(viewModel, appBar, snack, next = { stage ->
                Routes.dashboardNext(stage)?.let { route ->
                    nav.popClear(route)
                }
            }, back = {
                roiModel.clear()
                nav.popClear(Roi.Routes.list)
            }, vis = {
                nav.popClear(Routes.visualize)
            }, sep = {
                nav.popClear(Routes.separabilityDashboard)
            }, clazz = {
                nav.popClear(Routes.classification)
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
        b.composable(Routes.cont_cra) {
            ContemporaryCRA.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.hist_choice)
            }) {
                viewModel.clearLocalContemporaryCRA()
                nav.popClear(Routes.dashboard)
            }
        }
        b.composable(Routes.hist_choice) {
            ChooseHistorical.Screen(viewModel, {
                when(viewModel.state.value.historicalChoice) {
                    HistoricalChoice.SEPARATE -> {
                        nav.navigate(Routes.hist_cra)
                    }
                    else -> {
                        nav.navigate(Routes.cra_fields)
                    }
                }
            }) {
                viewModel.clearHistoricalChoice()
                nav.popBackStack()
            }
        }
        b.composable(Routes.hist_cra) {
            HistoricalCRA.Screen(viewModel, snack, {
                nav.navigate(Routes.cra_fields)
            }) {
                viewModel.clearLocalHistoricalCRA()
                nav.popBackStack()
            }
        }
        b.composable(Routes.cra_fields) {
            CRAFields.Screen(viewModel, snack, {
                viewModel.clear()
                nav.popClear(Routes.dashboard)
            }) {
                nav.popBackStack()
            }
        }
    }
}