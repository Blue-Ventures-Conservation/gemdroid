package org.blueventures.gemdroid.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

object Nav {
    @Composable
    fun Wrap(back: Click, content: @Composable () -> Unit) {
        content()

        BackHandler {
            back()
        }
    }
}