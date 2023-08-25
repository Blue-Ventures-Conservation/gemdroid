package org.blueventures.gemdroid.ui.analysis.cra

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.cra.HistoricalChoice
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.cra.screens.CRAFields
import org.blueventures.gemdroid.ui.analysis.cra.screens.ChooseHistorical
import org.blueventures.gemdroid.ui.analysis.cra.screens.ContemporaryCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.HistoricalCRA
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import java.net.HttpURLConnection

object CRA {
    object Routes {
        const val cont_cra = "analysis_cra_cont"
        const val hist_choice = "analysis_cra_hist_choice"
        const val hist_cra = "analysis_cra_hist"
        const val cra_fields = "analysis_cra_fields"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: CRAViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.cont_cra) {
            ContemporaryCRA.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.hist_choice)
            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }
        b.composable(Routes.hist_choice) {
            ChooseHistorical.Screen(viewModel, {
                when (viewModel.historicalChoice) {
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
                nav.popBackStack()
            }
        }
        b.composable(Routes.cra_fields) {
            CRAFields.Screen(viewModel, snack, {
                viewModel.clear()
                nav.popClear(Analysis.Routes.dashboard)
            }) {
                nav.popBackStack()
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