package org.blueventures.gemdroid.ui.analysis.cra

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.cra.HistoricalChoice
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.cra.screens.CRAFields
import org.blueventures.gemdroid.ui.analysis.cra.screens.ChooseHistorical
import org.blueventures.gemdroid.ui.analysis.cra.screens.ContemporaryCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.HistoricalCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.Purpose
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
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
        b.backHandler(Routes.purpose, nav::popBackStack) {
            Purpose.Screen(appBar) {
                nav.navigate(Routes.hist_choice)
            }
        }

        b.backHandler(Routes.hist_choice, {
            viewModel.clearHistoricalChoice()
            nav.popBackStack(Analysis.Routes.dashboard, false)
        }) {
            ChooseHistorical.Screen(viewModel, appBar) {
                when (viewModel.historicalChoice) {
                    HistoricalChoice.SEPARATE -> {
                        nav.navigate(Routes.hist_cra)
                    }
                    else -> {
                        nav.navigate(Routes.cont_cra)
                    }
                }
            }
        }

        b.backHandler(Routes.hist_cra, nav::popBackStack) {
            HistoricalCRA.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.cont_cra)
            }
        }

        b.backHandler(Routes.cont_cra, nav::popBackStack) {
            ContemporaryCRA.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.cra_fields)
            }
        }

        b.backHandler(Routes.cra_fields, nav::popBackStack) {
            CRAFields.Screen(viewModel, appBar, snack, nav::popBackStack) {
                viewModel.clearState()
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