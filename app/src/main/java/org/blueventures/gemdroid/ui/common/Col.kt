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
import androidx.compose.material3.Card
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
    inline fun BigPad(content: @Composable ColumnScope.() -> Unit) {
        Between(64.dp, 64.dp, 64.dp, content = content)
    }

    @Composable
    inline fun MidPad(content: @Composable ColumnScope.() -> Unit) {
        Between(start = 24.dp, end = 24.dp, bottom = 24.dp, content = content)
    }

    @Composable
    inline fun Between(start: Dp = 16.dp, top: Dp = 24.dp, end: Dp = 16.dp, bottom: Dp = 64.dp, content: @Composable ColumnScope.() -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start, top, end, bottom)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            content
        )
    }

    @Composable
    inline fun Dash(header: String, content: @Composable ColumnScope.() -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = header, fontSize = 20.sp)
            Spacer(modifier = Modifier.size(0.dp))
            content()
            Spacer(modifier = Modifier.size(0.dp))
        }
    }

    @Composable
    fun DashboardButton(label: String, click: Click) {
        Card(
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