package org.blueventures.gemdroid.ui.common

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

object Butt {
    @Composable
    fun Next(enabled: Boolean = true, onClick: () -> Unit) {
        Text("Next", enabled, onClick)
    }

    @Composable
    fun Done(enabled: Boolean = true, onClick: () -> Unit) {
        Text("Done", enabled, onClick)
    }

    @Composable
    fun Text(text: String, enabled: Boolean = true, onClick: () -> Unit) {
        Button(onClick, enabled = enabled) {
            Text(text, fontSize = 20.sp)
        }
    }
}