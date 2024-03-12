package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Checklist
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.once

object ScatterClasses {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        appBar.Update(AppBarUpdate(viewModel.title))
        GetRemote.AwaitSave(viewModel::loadScatterFile, viewModel::getScatter, viewModel::saveScatterFile, errorHandler = CRA::errHandler) { json ->
            val classes = JSONMap.classes(json)

            if (classes == null) {
                back.once()
                return@AwaitSave
            }

            Checklist.Screen(snack, stringResource(R.string.choose_classes), classes, true, 1) { checked ->
                viewModel.classes = checked
                next()
            }
        }
    }
}