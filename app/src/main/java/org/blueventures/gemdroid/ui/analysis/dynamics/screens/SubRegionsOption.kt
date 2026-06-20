package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.Once

object SubRegionsOption {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, snack: SnackFun, back: Click, content: @Composable () -> Unit) {
        Await.CRAOrGoBack(snack, back, stringResource(R.string.could_not_verify_cras).format(stringResource(R.string.dynamics)), viewModel.craAwaiter) { cra ->
            viewModel.cra = cra

            GetRemote.Save(
                getLocal = viewModel::loadDynamicsReadyFile,
                getRemote = viewModel::getDynamicsReady,
                save = viewModel::saveDynamicsReadyFile,
                errorHandler = Dynamics::errHandler
            ) { resp ->
                when {
                    resp.isReady() -> content()
                    else -> {
                        snack.Once(stringResource(R.string.please_wait_the_classification_is_being_saved))
                        back.Once()
                    }
                }
            }
        }
    }
}