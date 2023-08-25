package org.blueventures.gemdroid.ui.analysis.dynamics

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.SnackFun

object TargetClass {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras_dynamics), viewModel.craAwaiter) { cra ->
            Choice(viewModel, cra, appBar, next, back)
        }
    }

    @Composable
    fun Choice(viewModel: DynamicsViewModel, cra: CRA, appBar: AppBarFun, next: Click, back: Click) {
        appBar(AppBarUpdate())

        Col.Dash(stringResource(R.string.choose_class_dynamics), true) {
            cra.contemporaryCRA.stringClassValues.forEach {  classname ->
                DashboardButton(classname) {
                    viewModel.targetClass = classname
                    next()
                }
            }
        }

        BackHandler(onBack = back)
    }
}