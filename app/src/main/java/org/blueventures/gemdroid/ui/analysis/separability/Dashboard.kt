package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, separation: Click, scatter: Click, correlation: Click, back: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            SelectTimePeriod.DashboardButton(label = "Band Separation", separation)
            SelectTimePeriod.DashboardButton(label = "Band Scatter Plot", scatter)
            SelectTimePeriod.DashboardButton(label = "Band Correlation", correlation)
        }

        BackHandler {
            back()
        }
    }
}