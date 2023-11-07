package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object SubRegionsOption {
    fun screen(viewModel: DynamicsViewModel): @Composable (SnackFun, Click, @Composable () -> Unit) -> Unit {
        return { snack, back, content ->
            Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras).format(stringResource(R.string.dynamics)), viewModel.craAwaiter) { cra ->
                viewModel.cra = cra
                content()
            }
        }
    }
}