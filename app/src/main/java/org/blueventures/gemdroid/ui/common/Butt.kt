package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import org.blueventures.gemdroid.R

object Butt {
    @Composable
    fun Next(enabled: Boolean = true, click: Click) {
        Text(stringResource(R.string.next_button), enabled, click)
    }

    @Composable
    fun Done(enabled: Boolean = true, click: Click) {
        Text(stringResource(R.string.done_button), enabled, click)
    }

    @Composable
    fun Text(text: String, enabled: Boolean = true, click: Click) {
        Button(dropUnlessResumed(block = click), enabled = enabled) {
            Text(text, fontSize = 20.sp)
        }
    }
}

@Composable
fun Modifier.safeClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = clickable(enabled, onClick = dropUnlessResumed(block = onClick))