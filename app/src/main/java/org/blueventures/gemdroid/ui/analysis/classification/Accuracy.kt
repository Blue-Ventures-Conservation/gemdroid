package org.blueventures.gemdroid.ui.analysis.classification

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Accuracy {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, snack: SnackFun, accuracy: Click, back: Click) {
        BackHandler(onBack = back)
    }
}