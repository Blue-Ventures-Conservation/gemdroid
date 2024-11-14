package org.blueventures.gemdroid.ui.analysis.assess

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.assess.screens.Composites
import org.blueventures.gemdroid.ui.analysis.assess.screens.Description
import org.blueventures.gemdroid.ui.analysis.assess.screens.FitForPurpose
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.roi.Roi

object Assess {
    object Routes {
        const val prefix = "analysis_assess_"
        const val assess_description = prefix + "description"
        const val assess_composites = prefix + "composites"
        const val edits_needed = prefix + "edits_needed"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, roiViewModel: RoiViewModel, appBar: AppBar, snack: SnackFun) {
        b.backHandler(Routes.assess_description, {
            nav.popClear(Analysis.Routes.dashboard)
        }) { back ->
            Description.Screen(viewModel, roiViewModel, appBar, snack, back) {
                nav.navigate(Routes.assess_composites)
            }
        }

        b.backHandler(Routes.assess_composites, nav::popBackStack) {
            Composites.Screen(viewModel, appBar) {
                nav.navigate(Routes.edits_needed)
            }
        }

        b.backHandler(Routes.edits_needed, nav::popBackStack) {
            FitForPurpose.Screen(viewModel, roiViewModel, appBar, {
                nav.popClear(Analysis.Routes.dashboard)
            }) {
                nav.navigate(Roi.Routes.contemporaryYears)
            }
        }
    }
}