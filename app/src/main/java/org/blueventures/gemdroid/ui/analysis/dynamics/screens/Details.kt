package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click

object Details {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, back: Click) {
        appBar(AppBarUpdate(stringResource(R.string.dynamics)))
        Text(text = "details")
        BackHandler(onBack = back)
    }
}