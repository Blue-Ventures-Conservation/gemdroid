package org.blueventures.gemdroid.ui.roi

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click

object HistoricalMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, back: Click, next: Click) {
        Months.Screen(
            selector = object : Months.Selector {
                override val initMonthStart: Int = viewModel.historicalMonthStart
                override val initMonthEnd: Int = viewModel.historicalMonthEnd
                override fun setMonthStart(month: Int) { viewModel.historicalMonthStart = month }
                override fun setMonthEnd(month: Int) { viewModel.historicalMonthEnd = month }
            }, temporal = "Historical", back = back, next = next)
    }
}