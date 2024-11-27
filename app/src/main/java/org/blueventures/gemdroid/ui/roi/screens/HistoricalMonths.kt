package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object HistoricalMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, snack: SnackFun, next: Click) {
        Months.Screen(
            object : Months.Selector {
                override val selected = viewModel.historicalMonths

                override fun setMonths(months: List<Int>) {
                    viewModel.historicalMonths = months
                }
            }, stringResource(R.string.historical), appBar, snack, next
        )
    }
}