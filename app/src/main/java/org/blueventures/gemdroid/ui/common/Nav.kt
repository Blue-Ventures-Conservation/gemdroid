package org.blueventures.gemdroid.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

object Nav {
    interface Navver {
        fun next()
        fun back()
    }

    @Composable
    fun Wrap(back: Click, next: Click = {}, content: @Composable (Navver) -> Unit) {
        val (wentBack, setWentBack) = remember { mutableStateOf(false) }
        val (wentNext, setWentNext) = remember { mutableStateOf(false) }

        val navver = object : Navver {
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
            content(navver)
        }

        BackHandler {
            navver.back()
        }
    }
}