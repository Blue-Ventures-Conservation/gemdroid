package org.blueventures.gemdroid.ui.analysis.assess.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.SnackFun

object FitForPurpose {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, roiViewModel: RoiViewModel, appBar: AppBar, snack: SnackFun, yes: Click, no: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.assess_imagery)))

        val context = LocalContext.current.applicationContext
        Col.Dash(stringResource(R.string.do_your_composites_look_fit_for_purpose)) {
            Col.DashboardButton(stringResource(R.string.yes_move_on_to_classification)) {
                viewModel.saveCompositesAssessedFile { result ->
                    when {
                        result.isFailure ->snack(context.getString(R.string.failed_to_save_please_try_again))
                        else -> yes()
                    }
                }
            }
            Col.DashboardButton(stringResource(R.string.no_let_me_edit_the_years_and_months)) {
                roiViewModel.importROI("", viewModel.roi)
                roiViewModel.isAssessmentEditor = true
                no()
            }
        }
    }
}