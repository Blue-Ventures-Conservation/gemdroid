package org.blueventures.gemdroid.ui.analysis.classification

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.analysis.classification.screens.Details
import org.blueventures.gemdroid.ui.analysis.classification.screens.Downloads
import org.blueventures.gemdroid.ui.analysis.classification.screens.Map
import org.blueventures.gemdroid.ui.analysis.classification.separability.Separability
import org.blueventures.gemdroid.ui.analysis.classification.separability.Separability.Routes.timePeriod
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import java.net.HttpURLConnection

object Classification {
    object Routes {
        const val map = "analysis_classification_map"
        const val details = "analysis_classification_details"
        const val downloads = "analysis_classification_downloads"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: ClassificationViewModel, appBar: AppBar, snack: SnackFun) {
        Separability.screens(b, nav, viewModel.sepViewModel, appBar, snack)

        b.composable(Routes.map) {
            Map.Screen(viewModel, appBar, snack, nav::popBackStack) {
                nav.navigate(Routes.details)
            }
        }

        b.composable(Routes.details) {
            Details.Screen(viewModel, appBar, nav::popBackStack, {
                nav.navigate(timePeriod)
            }) {
                nav.navigate(Routes.downloads)
            }
        }

        b.composable(Routes.downloads) {
            Downloads.Screen(viewModel, appBar, nav::popBackStack)
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        val craErr = CRA.errHandler(ctx, code, message)
        return if (craErr.first != null) {
            craErr
        } else if (code == HttpURLConnection.HTTP_BAD_REQUEST && message?.contains("classifier training failed") == true) {
            Pair(ctx.getString(R.string.classifier_training_failed), false)
        } else {
            Pair(null, true)
        }
    }
}