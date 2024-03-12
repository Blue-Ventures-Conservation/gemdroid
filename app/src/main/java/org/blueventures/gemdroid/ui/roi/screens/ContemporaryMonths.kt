package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click

object ContemporaryMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, next: Click) {
        Months.Screen(
            object : Months.Selector {
                override val initMonthStart: Int = viewModel.contemporaryMonthStart
                override val initMonthEnd: Int = viewModel.contemporaryMonthEnd
                override fun setMonthStart(month: Int) {
                    viewModel.contemporaryMonthStart = month
                }

                override fun setMonthEnd(month: Int) {
                    viewModel.contemporaryMonthEnd = month
                }
            }, stringResource(R.string.contemporary), appBar, next
        )
    }
}