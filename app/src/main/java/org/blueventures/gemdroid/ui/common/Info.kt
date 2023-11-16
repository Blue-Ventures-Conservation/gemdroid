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
import androidx.compose.material3.Divider
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
    @Composable
    fun BlueLine() = Divider(color = SkyBlue, thickness = 1.dp)

    @Composable
    fun Space() = Spacer(modifier = Modifier.height(16.dp))

    @Composable
    fun Header(title: String) {
        Column {
            Text(text = title, fontSize = 32.sp, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            BlueLine()
        }
    }

    @Composable
    fun SubHeader(title: String) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp)) {
            Text(text = title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            BlueLine()
        }
    }

    @Composable
    fun Block(content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = Modifier.padding(bottom = 16.dp), content = content)
    }

    @Composable
    fun Txt(text: String, fontSize: TextUnit = 20.sp) = Text(text, fontSize = fontSize)

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