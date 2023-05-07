package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, separation: Click, scatter: Click, correlation: Click, back: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Select a chart to view:", fontSize = 20.sp)
            Spacer(modifier = Modifier.size(0.dp))
            SelectTimePeriod.DashboardButton(label = "Band Separation", separation)
            SelectTimePeriod.DashboardButton(label = "Band Scatter Plot", scatter)
            SelectTimePeriod.DashboardButton(label = "Band Correlation", correlation)
            Spacer(modifier = Modifier.size(0.dp))
        }

        BackHandler {
            back()
        }
    }
}