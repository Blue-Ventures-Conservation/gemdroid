package org.blueventures.gemdroid.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

object Orient {
    @Composable
    fun Portrait() {
        Lock(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    @Composable
    fun Landscape() {
        Lock(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    }

    @Composable
    private fun Lock(orientation: Int) {
        val context = LocalContext.current
        DisposableEffect(orientation) {
            val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}