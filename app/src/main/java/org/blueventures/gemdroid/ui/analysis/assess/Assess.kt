package org.blueventures.gemdroid.ui.analysis.assess

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.assess.screens.Composites
import org.blueventures.gemdroid.ui.analysis.assess.screens.Description
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.backHandler

object Assess {
    object Routes {
        const val prefix = "analysis_assess_"
        const val assess_description = prefix + "description"
        const val assess_composites = prefix + "composites"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, appBar: AppBar) {
        b.backHandler(Routes.assess_description, nav::popBackStack) {
            Description.Screen(appBar) {
                nav.navigate(Routes.assess_composites)
            }
        }

        b.backHandler(Routes.assess_composites, nav::popBackStack) {
            Composites.Screen(viewModel, appBar) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }
    }
}