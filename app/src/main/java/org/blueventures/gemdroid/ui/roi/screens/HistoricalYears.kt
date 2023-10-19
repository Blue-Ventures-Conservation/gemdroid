package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object HistoricalYears {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Years.Screen(
            object : Years.Selector {
                override val initYearStart: Int = viewModel.historicalYearStart
                override val initYearEnd: Int = viewModel.historicalYearEnd
                override fun setYearStart(year: Int) {
                    viewModel.historicalYearStart = year
                }

                override fun setYearEnd(year: Int) {
                    viewModel.historicalYearEnd = year
                }

                override fun validateOrder(): Boolean = viewModel.validateHistoricalYearsOrder()
                override fun validateGap(): Boolean = viewModel.validateHistoricalYearsGap()
            }, stringResource(R.string.historical), viewModel.currentYear(), appBar, snack, back, next
        )
    }
}