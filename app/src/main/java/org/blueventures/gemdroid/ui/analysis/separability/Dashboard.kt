package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, separation: Click, scatter: Click, correlation: Click, back: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        Col.Dash("Select a chart to view:") {
            DashboardButton(label = "Band Separation", separation)
            DashboardButton(label = "Band Scatter Plot", scatter)
            DashboardButton(label = "Band Correlation", correlation)
        }

        BackHandler {
            back()
        }
    }
}