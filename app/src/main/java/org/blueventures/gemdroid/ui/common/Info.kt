package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Info {
    @Composable
    fun Header(title: String) {
        Column {
            Text(text = title, fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
            Divider(color = SkyBlue, thickness = 1.dp)
        }
    }

    @Composable
    fun Block(content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = Modifier.padding(bottom = 16.dp), content = content)
    }

    @Composable
    fun Row(content: @Composable RowScope.() -> Unit) {
        Row(modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically)
        {
            content()
        }
    }
}