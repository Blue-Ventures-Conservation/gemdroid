package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.chargemap.compose.numberpicker.NumberPicker
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.SnackFun

object Months {
    interface Selector {
        val initMonthStart: Int
        val initMonthEnd: Int

        fun setMonthStart(month: Int)
        fun setMonthEnd(month: Int)
        fun validateMonths(): Boolean
    }

    @Composable
    fun Screen(selector: Selector, temporal: String, snack: SnackFun, back: Click, next: Click) {
        Col.BigPad {
            Text("Select range of months (inclusive) for $temporal imagery:", textAlign = TextAlign.Center, fontSize = 24.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectMonth(initMonth = selector.initMonthStart, setMonth = selector::setMonthStart)
                SelectMonth(initMonth = selector.initMonthEnd, setMonth = selector::setMonthEnd)
            }
            Butt.Next {
                if (selector.validateMonths()) {
                    next()
                } else {
                    snack("Month on the left must be equal to or less than the one on the right")
                }
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun SelectMonth(initMonth: Int, setMonth: (Int) -> Unit) {
        var month by remember { mutableStateOf(initMonth) }
        NumberPicker(
            value = month,
            range = 1..12,
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                setMonth(it)
                month = it
            }
        )
    }
}