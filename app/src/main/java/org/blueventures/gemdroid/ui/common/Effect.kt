package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope

object Effect {
    @Composable
    fun Once(block: suspend CoroutineScope.() -> Unit) {
        LaunchedEffect(true, block)
    }
}

@Composable
fun Click.once() {
    val click = this
    Effect.Once {
        click()
    }
}

@Composable
fun SnackFun.once(msg: String) {
    val snack = this
    Effect.Once {
        snack(msg)
    }
}