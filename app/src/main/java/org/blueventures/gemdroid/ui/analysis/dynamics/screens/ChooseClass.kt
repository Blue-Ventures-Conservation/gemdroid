package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object ChooseClass {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))

        Col.Dash(stringResource(R.string.choose_which_class_to_analyze)) {
            viewModel.analysisRegion.allClasses.forEach { classDynamics ->
                DashboardButton(classDynamics.name) {
                    viewModel.analysisClass = classDynamics
                    next()
                }
            }
        }
    }
}