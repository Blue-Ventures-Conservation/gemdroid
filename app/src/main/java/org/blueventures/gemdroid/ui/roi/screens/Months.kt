package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Checklist
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Months {
    private val resources = listOf(R.string.january, R.string.february, R.string.march, R.string.april, R.string.may, R.string.june, R.string.july, R.string.august, R.string.september, R.string.october, R.string.november, R.string.december)

    interface Selector {
        val selected: List<Int>
        fun setMonths(months: List<Int>)
    }

    @Composable
    fun Screen(selector: Selector, temporal: String, appBar: AppBar, snack: SnackFun, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.create_project)))
        val months = mutableListOf<String>()
        for (res in resources) {
            months.add(stringResource(res))
        }

        val initState = MutableList(12) { false }
        for (month in selector.selected) {
            initState[month-1] = true
        }

        Checklist.Screen(snack, title = "$temporal ${stringResource(R.string.select_months_range)}", items = months, initState = initState) { chosen ->
            val chosenInts = mutableListOf<Int>()
            for (month in chosen) {
                chosenInts.add(months.indexOf(month) + 1)
            }
            selector.setMonths(chosenInts)
            next()
        }
    }
}