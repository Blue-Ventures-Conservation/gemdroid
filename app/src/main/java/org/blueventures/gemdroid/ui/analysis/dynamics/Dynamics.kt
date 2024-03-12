package org.blueventures.gemdroid.ui.analysis.dynamics

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.ChooseClass
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.ChooseRegion
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.CombinedName
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Downloads
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.DynamicsMap
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.Stats
import org.blueventures.gemdroid.ui.analysis.dynamics.screens.TargetClasses
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object Dynamics {
    object Routes {
        const val prefix = "analysis_dynamics_"
        const val target_classes = prefix + "target_classes"
        const val combined_name = prefix + "combined_name"
        const val map = prefix + "map"
        const val choose_region = prefix + "choose_region"
        const val downloads = prefix + "downloads"
        const val choose_class = prefix + "choose_class"
        const val stats = prefix + "stats"
    }

    private fun finalizeSubregions(viewModel: DynamicsViewModel) {
        viewModel.saveSubRegionsFile()
        viewModel.subregionsFinalized = true
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: DynamicsViewModel, appBar: AppBar, snack: SnackFun) {
        Polygons.screens(b, nav, Routes.prefix, Analysis.Routes.dashboard, Routes.target_classes, appBar, snack, viewModel)

        b.backHandler(Routes.target_classes, {
            if (viewModel.subregionsFinalized) nav.popBackStack(Analysis.Routes.dashboard, false) else nav.popBackStack()
        }) {
            TargetClasses.Screen(viewModel, appBar, snack) {
                when {
                    viewModel.combinedNameNeeded() -> nav.navigate(Routes.combined_name)
                    else -> {
                        finalizeSubregions(viewModel)
                        nav.navigate(Routes.map)
                    }
                }
            }
        }

        b.backHandler(Routes.combined_name, nav::popBackStack) {
            CombinedName.Screen(viewModel, appBar, snack) {
                finalizeSubregions(viewModel)
                nav.navigate(Routes.map)
            }
        }

        b.backHandler(Routes.map, {
            nav.popBackStack(Routes.target_classes, false)
        }) {
            DynamicsMap.Screen(viewModel, appBar) {
                nav.navigate(Routes.choose_region)
            }
        }

        b.backHandler(Routes.choose_region, nav::popBackStack) {
            ChooseRegion.Screen(viewModel, appBar, {
                nav.navigate(Routes.downloads)
            }) {
                nav.navigate(Routes.choose_class)
            }
        }

        b.backHandler(Routes.downloads, nav::popBackStack) { back ->
            Downloads.Screen(viewModel, appBar, back)
        }

        b.backHandler(Routes.choose_class, nav::popBackStack) {
            ChooseClass.Screen(viewModel, appBar) {
                nav.navigate(Routes.stats)
            }
        }

        b.backHandler(Routes.stats, nav::popBackStack) {
            Stats.Screen(viewModel, appBar)
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        return Classification.errHandler(ctx, code, message)
    }
}