package org.blueventures.gemdroid.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

object Nav {
    interface Navigator {
        fun next()
        fun back()
    }

    @Composable
    fun Wrap(back: Click, next: Click = {}, content: @Composable (Navigator) -> Unit) {
        val (wentBack, setWentBack) = remember { mutableStateOf(false) }
        val (wentNext, setWentNext) = remember { mutableStateOf(false) }

        val nav = object : Navigator {
            override fun next() {
                setWentNext(true)
                next()
            }

            override fun back() {
                setWentBack(true)
                back()
            }
        }

        if (!wentBack && !wentNext) {
            content(nav)
        }

        BackHandler {
            nav.back()
        }
    }
}