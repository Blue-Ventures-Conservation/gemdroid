package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click

object HistoricalMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBarFun, back: Click, next: Click) {
        Months.Screen(
            object : Months.Selector {
                override val initMonthStart: Int = viewModel.historicalMonthStart
                override val initMonthEnd: Int = viewModel.historicalMonthEnd
                override fun setMonthStart(month: Int) {
                    viewModel.historicalMonthStart = month
                }

                override fun setMonthEnd(month: Int) {
                    viewModel.historicalMonthEnd = month
                }
            }, stringResource(R.string.historical), appBar, back, next
        )
    }
}