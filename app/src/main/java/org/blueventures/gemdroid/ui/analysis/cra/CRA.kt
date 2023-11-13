package org.blueventures.gemdroid.ui.analysis.cra

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.cra.HistoricalChoice
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.Analysis.Routes.dashboard
import org.blueventures.gemdroid.ui.analysis.cra.screens.CRAFields
import org.blueventures.gemdroid.ui.analysis.cra.screens.ChooseHistorical
import org.blueventures.gemdroid.ui.analysis.cra.screens.ContemporaryCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.HistoricalCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.Purpose
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import java.net.HttpURLConnection

object CRA {
    object Routes {
        const val prefix = "analysis_cra_"
        const val purpose = prefix + "purpose"
        const val cont_cra = prefix + "cont"
        const val hist_choice = prefix + "hist_choice"
        const val hist_cra = prefix + "hist"
        const val cra_fields = prefix + "fields"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: CRAViewModel, appBar: AppBar, snack: SnackFun) {
        b.composable(Routes.purpose) {
            Purpose.Screen(appBar, nav::popBackStack) {
                nav.navigate(Routes.cont_cra)
            }
        }
        b.composable(Routes.cont_cra) {
            ContemporaryCRA.Screen(viewModel, appBar, snack, {
                nav.popBackStack(dashboard, false)
            }) {
                nav.navigate(Routes.hist_choice)
            }
        }
        b.composable(Routes.hist_choice) {
            ChooseHistorical.Screen(viewModel, appBar, {
                viewModel.clearHistoricalChoice()
                nav.popBackStack()
            }) {
                when (viewModel.historicalChoice) {
                    HistoricalChoice.SEPARATE -> {
                        nav.navigate(Routes.hist_cra)
                    }
                    else -> {
                        nav.navigate(Routes.cra_fields)
                    }
                }
            }
        }
        b.composable(Routes.hist_cra) {
            HistoricalCRA.Screen(viewModel, appBar, snack, nav::popBackStack) {
                nav.navigate(Routes.cra_fields)
            }
        }
        b.composable(Routes.cra_fields) {
            CRAFields.Screen(viewModel, appBar, snack, nav::popBackStack) {
                viewModel.clear()
                nav.popClear(Analysis.Routes.dashboard)
            }
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        return if (code == HttpURLConnection.HTTP_BAD_REQUEST && message?.contains("missing asset") == true) {
            Pair(ctx.getString(R.string.cra_not_found), false)
        } else {
            Pair(null, true)
        }
    }
}