package org.blueventures.gemdroid.ui.roi.screens

import androidx.activity.compose.BackHandler
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
import com.chargemap.compose.numberpicker.ListItemPicker
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col

object Months {
    private val resources = listOf(R.string.january, R.string.february, R.string.march, R.string.april, R.string.may, R.string.june, R.string.july, R.string.august, R.string.september, R.string.october, R.string.november, R.string.december)

    interface Selector {
        val initMonthStart: Int
        val initMonthEnd: Int

        fun setMonthStart(month: Int)
        fun setMonthEnd(month: Int)
    }

    @Composable
    fun Screen(selector: Selector, temporal: String, back: Click, next: Click) {
        val months = mutableListOf<String>()
        for (res in resources) {
            months.add(stringResource(res))
        }

        Col.BigPad {
            Text("Select range of months (inclusive) for $temporal imagery:", textAlign = TextAlign.Center, fontSize = 24.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectMonth(months, selector.initMonthStart, selector::setMonthStart)
                SelectMonth(months, selector.initMonthEnd, selector::setMonthEnd)
            }
            Butt.Next {
                next()
            }
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun SelectMonth(months: List<String>, initMonth: Int, setMonth: (Int) -> Unit) {
        var month by remember { mutableStateOf(months[initMonth-1]) }
        ListItemPicker(
            value = month,
            list = months,
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                setMonth(months.indexOf(it)+1)
                month = it
            }
        )
    }
}