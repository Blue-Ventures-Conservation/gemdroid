package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable

class AppBar(private val barFun: @Composable (AppBarUpdate) -> Unit) {
    @Composable
    fun Update(update: AppBarUpdate) = barFun(update)
}