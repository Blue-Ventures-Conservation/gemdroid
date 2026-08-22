package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ClickContent(val click: Click, val content: @Composable () -> Unit)
@Composable
fun BoxScope.FloatingButton(click: Click, align: Alignment = Alignment.BottomEnd, content: @Composable () -> Unit) {
    FloatingActionButton(click, modifier = Modifier
        .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp)
        .align(align),
        content = content,
    )
}

@Composable
fun BoxScope.FloatingButtons(vararg clickContents: ClickContent) {
    Column(Modifier
        .padding(16.dp, 12.dp, 16.dp, 24.dp)
        .align(Alignment.BottomEnd),
        Arrangement.spacedBy(16.dp, Alignment.Bottom)) {
        for (clickContent in clickContents) {
            FloatingActionButton(clickContent.click, content = clickContent.content)
        }
    }
}