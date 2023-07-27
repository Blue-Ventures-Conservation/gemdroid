package org.blueventures.gemdroid.ui.analysis.classification

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.classification.separability.Separability
import org.blueventures.gemdroid.ui.analysis.classification.separability.Separability.Routes.timePeriod
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun
import java.net.HttpURLConnection

object Classification {
    object Routes {
        const val classification_map = "analysis_classification_map"
        const val details = "analysis_classification_details"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: ClassificationViewModel, appBar: AppBarFun, snack: SnackFun) {
        Separability.screens(b, nav, viewModel.sepViewModel, appBar, snack)

        b.composable(Routes.classification_map) {
            Map.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.details)
            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }

        b.composable(Routes.details) {
            Details.Screen(viewModel, appBar, snack, {
                nav.navigate(timePeriod)
            }) {
                nav.popBackStack()
            }
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        return if (code == HttpURLConnection.HTTP_BAD_REQUEST && message?.contains("classifier training failed") == true) {
            Pair(ctx.getString(R.string.classifier_training_failed), false)
        } else {
            Pair(null, true)
        }
    }
}