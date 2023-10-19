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
import org.blueventures.gemdroid.ui.common.Nav

object TargetClass {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, next: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
            Col.Dash(stringResource(R.string.choose_class_dynamics), true) {
                viewModel.cra.contemporaryCRA.stringClassValues.forEach { classname ->
                    DashboardButton(classname) {
                        viewModel.targetClass = classname
                        next()
                    }
                }
            }
        }
    }
}
