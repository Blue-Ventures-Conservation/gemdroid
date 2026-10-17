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
import androidx.lifecycle.compose.dropUnlessResumed

data class ClickContent(val click: Click, val content: @Composable () -> Unit)
@Composable
fun BoxScope.FloatingButton(click: Click, align: Alignment = Alignment.BottomEnd, content: @Composable () -> Unit) {
    FloatingActionButton(dropUnlessResumed(block = click), modifier = Modifier
        .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp)
        .align(align),
        content = content,
    )
}

@Composable
fun BoxScope.FloatingButtons(alignment: Alignment = Alignment.BottomEnd, contents: List<ClickContent>) {
    Column(Modifier
        .padding(16.dp, 12.dp, 16.dp, 24.dp)
        .align(alignment),
        Arrangement.spacedBy(16.dp)) {
        for (cc in contents) {
            FloatingActionButton(cc.click, content = cc.content)
        }
    }
}