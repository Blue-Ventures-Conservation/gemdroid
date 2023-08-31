package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.chargemap.compose.numberpicker.NumberPicker
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun

object Years {
    interface Selector {
        val initYearStart: Int
        val initYearEnd: Int

        fun setYearStart(year: Int)
        fun setYearEnd(year: Int)
        fun validateOrder(): Boolean
        fun validateGap(): Boolean
    }

    @Composable
    fun Screen(selector: Selector, temporal: String, currentYear: Int, appBar: AppBarFun, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back, next) { nav ->
            appBar(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
            Col.BigPad {
                Text(stringResource(R.string.select_bounding_years).format(temporal), textAlign = TextAlign.Center, fontSize = 24.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectYear(selector.initYearStart, currentYear, selector::setYearStart)
                    SelectYear(selector.initYearEnd, currentYear, selector::setYearEnd)
                }
                val gapMsg = stringResource(R.string.please_select_years_within).format(RoiViewModel.maxYearGap.toString())
                val orderMsg = stringResource(R.string.year_compare_fail)
                Butt.Next {
                    if (selector.validateOrder()) {
                        if (selector.validateGap()) {
                            nav.next()
                        } else {
                            snack(gapMsg)
                        }
                    } else {
                        snack(orderMsg)
                    }
                }
            }
        }
    }

    @Composable
    fun SelectYear(initYear: Int, currentYear: Int, setYear: (Int) -> Unit) {
        var year by remember { mutableStateOf(initYear) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..currentYear,
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                setYear(it)
                year = it
            }
        )
    }
}