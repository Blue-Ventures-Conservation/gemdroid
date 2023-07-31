package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Col {
    @Composable
    inline fun BigPad(arrange: Arrangement.Vertical = Arrangement.SpaceBetween, fill: Boolean = true, scroll: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
        Col(64.dp, 64.dp, 64.dp, arrange = arrange, fill = fill, scroll = scroll, content = content)
    }

    @Composable
    inline fun MidPad(arrange: Arrangement.Vertical = Arrangement.SpaceBetween, fill: Boolean = true, scroll: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
        Col(start = 24.dp, end = 24.dp, bottom = 24.dp, arrange = arrange, fill = fill, scroll = scroll, content = content)
    }

    @Composable
    inline fun Col(start: Dp = 16.dp, top: Dp = 24.dp, end: Dp = 16.dp, bottom: Dp = 64.dp, arrange: Arrangement.Vertical = Arrangement.SpaceBetween, fill: Boolean = true, scroll: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
        var modifier = Modifier
            .padding(start, top, end, bottom)

        if (fill) {
            modifier = modifier.fillMaxSize()
        }

        if (scroll) {
            modifier = modifier.verticalScroll(rememberScrollState())
        }
        Column(
            modifier = modifier,
            verticalArrangement = arrange,
            horizontalAlignment = Alignment.CenterHorizontally,
            content
        )
    }

    @Composable
    inline fun Dash(header: String, content: @Composable ColumnScope.() -> Unit) {
        Col(
            arrange = Arrangement.SpaceAround,
        ) {
            Text(text = header, fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.size(0.dp))
            content()
            Spacer(modifier = Modifier.size(0.dp))
        }
    }

    @Composable
    fun DashboardButton(label: String, click: Click) {
        Surface(
            border = BorderStroke(2.dp, SkyBlue),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { click() },
        ) {
            Text(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), text = label, fontSize = 20.sp, textAlign = TextAlign.Center)
        }
    }
}