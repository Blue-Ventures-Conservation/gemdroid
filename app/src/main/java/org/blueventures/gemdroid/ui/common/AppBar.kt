package org.blueventures.gemdroid.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class AppBar(private val barFun: @Composable (AppBarUpdate) -> Unit) {
    @Composable
    fun Update(update: AppBarUpdate) = barFun(update)

    @Composable
    fun Title(@StringRes id: Int) {
        Update(AppBarUpdate(stringResource(id)))
    }
}