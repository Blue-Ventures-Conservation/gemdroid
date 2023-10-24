package org.blueventures.gemdroid.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

object Nav {
    @Composable
    fun Wrap(back: Click, portrait: Boolean = false, content: @Composable () -> Unit) {
        if (portrait) {
            Orient.Portrait()
        }

        content()

        BackHandler {
            back()
        }
    }
}