package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Info {
    val txtSize = 20.sp

    @Composable
    fun BlueLine() = HorizontalDivider(color = SkyBlue, thickness = 1.dp)

    @Composable
    fun Space() = Spacer(modifier = Modifier.height(16.dp))

    @Composable
    fun Header(title: String, truncate: Boolean = true) {
        val maxLines = if (truncate) 1 else Int.MAX_VALUE
        Column {
            Text(text = title, fontSize = 32.sp, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp), maxLines = maxLines, overflow = TextOverflow.Ellipsis)
            BlueLine()
            BlueLine()
        }
    }

    @Composable
    fun SubHeader(title: String, truncate: Boolean = true) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp)) {
            Txt(text = title, truncate = truncate)
            BlueLine()
        }
    }

    @Composable
    fun Block(content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = Modifier.padding(bottom = 16.dp), content = content)
    }

    @Composable
    fun Txt(text: String, fontSize: TextUnit = txtSize, truncate: Boolean = false) {
        val maxLines = if (truncate) 1 else Int.MAX_VALUE
        Text(text, fontSize = fontSize, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }

    @Composable
    fun Row(verticalPadding: Dp = 4.dp, enabled: Boolean = false, click: Click = {}, content: @Composable RowScope.() -> Unit) {
        Row(modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = verticalPadding, bottom = verticalPadding)
            .fillMaxWidth().clickable(enabled = enabled, onClick = click),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically)
        {
            content()
        }
    }
}