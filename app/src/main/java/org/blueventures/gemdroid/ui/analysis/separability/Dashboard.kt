package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        appBar(AppBarUpdate(title = "Spectral Separability"))

        val (cras, setCRAs) = remember { mutableStateOf<Result<CRA>?>(null) }

        when {
            cras == null -> {
                PleaseWait()
                viewModel.loadCRAs(setCRAs)
            }
            cras.isFailure -> {
                snack(cras.exceptionOrNull()!!.message!!)
            }
            else -> {
                snack("${cras.getOrNull()!!.historicalCRA == null}")
            }
        }

        BackHandler {
            back()
        }
    }
}