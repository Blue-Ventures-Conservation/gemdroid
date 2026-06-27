package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.SeparabilityJSON
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Checklist
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Once
import org.blueventures.gemdroid.ui.common.SnackFun

object ScatterClasses {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        appBar.Update(AppBarUpdate(viewModel.title))
        GetRemote.Save(viewModel::loadScatterFile, viewModel::getScatter, viewModel::saveScatterFile, errorHandler = CRA::errHandler) { json ->
            val classes = SeparabilityJSON.classes(json)

            if (classes == null) {
                back.Once()
                return@Save
            }

            Checklist.Screen(snack, stringResource(R.string.choose_classes), classes, List(classes.size) {true}) { checked ->
                viewModel.classes = checked
                next()
            }
        }
    }
}