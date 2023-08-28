package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.SnackFun

object TargetClass {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, next: Click, back: Click) {
        appBar(AppBarUpdate(stringResource(R.string.dynamics)))
        Col.Dash(stringResource(R.string.choose_class_dynamics), true) {
            viewModel.cra.contemporaryCRA.stringClassValues.forEach { classname ->
                DashboardButton(classname) {
                    viewModel.targetClass = classname
                    next()
                }
            }
        }
        BackHandler(onBack = back)
    }
}
