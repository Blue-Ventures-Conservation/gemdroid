package org.blueventures.gemdroid.ui.roi

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object ContemporaryMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Months.Screen(
            selector = object : Months.MonthSelector {
                override val initMonthStart: Int = viewModel.contemporaryMonthStart
                override val initMonthEnd: Int = viewModel.contemporaryMonthEnd
                override fun setMonthStart(month: Int) { viewModel.contemporaryMonthStart = month }
                override fun setMonthEnd(month: Int) { viewModel.contemporaryMonthEnd = month }
                override fun validateMonths(): Boolean = viewModel.validateContemporaryMonthsOrder()
            }, temporal = "Contemporary", snack = snack, back = back, next = next)
    }
}