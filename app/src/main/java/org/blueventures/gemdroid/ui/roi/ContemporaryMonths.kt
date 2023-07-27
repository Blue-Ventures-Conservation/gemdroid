package org.blueventures.gemdroid.ui.roi

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click

object ContemporaryMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, back: Click, next: Click) {
        Months.Screen(
            selector = object : Months.Selector {
                override val initMonthStart: Int = viewModel.contemporaryMonthStart
                override val initMonthEnd: Int = viewModel.contemporaryMonthEnd
                override fun setMonthStart(month: Int) { viewModel.contemporaryMonthStart = month }
                override fun setMonthEnd(month: Int) { viewModel.contemporaryMonthEnd = month }
            }, temporal = "Contemporary", back = back, next = next)
    }
}