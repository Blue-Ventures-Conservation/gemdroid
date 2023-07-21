package org.blueventures.gemdroid.ui.analysis.cra

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.analysis.cra.CRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object ContemporaryCRA {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        appBar(AppBarUpdate(title = "Classification Reference Areas (CRAs)"))
        CRA.Screen(viewModel, "Contemporary", snack, next, back, null, { cra ->
            viewModel.contemporaryCRA = cra
        }) { key ->
            viewModel.contemporaryCRA = CRAFile(storageKey = key)
        }
    }
}