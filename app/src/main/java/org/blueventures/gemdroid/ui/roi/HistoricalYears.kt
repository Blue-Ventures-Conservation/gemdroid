package org.blueventures.gemdroid.ui.roi

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object HistoricalYears {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Years.Screen(
            selector = object : Years.Selector {
                override val initYearStart: Int = viewModel.historicalYearStart
                override val initYearEnd: Int = viewModel.historicalYearEnd
                override fun setYearStart(year: Int) { viewModel.historicalYearStart = year }
                override fun setYearEnd(year: Int) { viewModel.historicalYearEnd = year }
                override fun validateOrder(): Boolean = viewModel.validateHistoricalYearsOrder()
                override fun validateGap(): Boolean = viewModel.validateHistoricalYearsGap()
            }, temporal = "Historical", currentYear = viewModel.currentYear(), snack = snack, back = back, next = next)
    }
}