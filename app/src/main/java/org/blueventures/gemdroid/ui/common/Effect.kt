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