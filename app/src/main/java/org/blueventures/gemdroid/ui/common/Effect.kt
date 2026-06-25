package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope

// TODO: evaluate usages of this, as it is misleading. The launched block can execute more than once any time
// the composable leaves and reenters the composition, like on rotation. There should be a smarter key to use
// in many cases. Places where this is used to ask the viewmodel to make an api call should move the control
// of making sure that happens once entirely inside the viewmodel and not rely on composables for that level
// of management. For making sure a Click happens once, we should build some logic to track whether a Click
// has already been invoked into a wrapper that uses something like:
//                     var alreadyClicked by remember { mutableStateOf(false) }
// to prevent further invocations from executing.
object Effect {
    @Composable
    fun Once(block: suspend CoroutineScope.() -> Unit) {
        LaunchedEffect(true, block)
    }
}

@Composable
fun Click.Once() {
    val click = this
    Effect.Once {
        click()
    }
}

@Composable
fun SnackFun.Once(msg: String) {
    val snack = this
    Effect.Once {
        snack(msg)
    }
}