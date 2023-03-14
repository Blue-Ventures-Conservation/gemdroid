package org.blueventures.gemdroid.ui.roi

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object HistoricalMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Months.Screen(
            selector = object : Months.Selector {
                override val initMonthStart: Int = viewModel.historicalMonthStart
                override val initMonthEnd: Int = viewModel.historicalMonthEnd
                override fun setMonthStart(month: Int) { viewModel.historicalMonthStart = month }
                override fun setMonthEnd(month: Int) { viewModel.historicalMonthEnd = month }
                override fun validateMonths(): Boolean = viewModel.validateHistoricalMonthsOrder()
            }, temporal = "Historical", snack = snack, back = back, next = next)
    }
}