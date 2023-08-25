package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object ContemporaryYears {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Years.Screen(
            selector = object : Years.Selector {
                override val initYearStart: Int = viewModel.contemporaryYearStart
                override val initYearEnd: Int = viewModel.contemporaryYearEnd
                override fun setYearStart(year: Int) {
                    viewModel.contemporaryYearStart = year
                }

                override fun setYearEnd(year: Int) {
                    viewModel.contemporaryYearEnd = year
                }

                override fun validateOrder(): Boolean = viewModel.validateContemporaryYearsOrder()
                override fun validateGap(): Boolean = viewModel.validateContemporaryYearsGap()
            }, temporal = "Contemporary", currentYear = viewModel.currentYear(), snack = snack, back = back, next = next
        )
    }
}