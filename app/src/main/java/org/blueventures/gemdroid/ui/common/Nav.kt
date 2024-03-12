package org.blueventures.gemdroid.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

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

fun NavGraphBuilder.backHandler(route: String, back: Click, portrait: Boolean = false, content: @Composable (Click) -> Unit) {
    composable(route) {
        Nav.Wrap(back, portrait) {
            content(back)
        }
    }
}