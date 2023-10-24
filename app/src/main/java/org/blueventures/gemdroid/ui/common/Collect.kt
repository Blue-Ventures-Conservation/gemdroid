package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object Collect {
    @Composable
    fun Text(header: String, label: String, initial: String, snack: SnackFun, validator: (String) -> String?, next: Click) {
        Col.Col {
            Text(text = header, fontSize = 24.sp, textAlign = TextAlign.Center)
            val focus = LocalFocusManager.current
            var text by remember { mutableStateOf(initial) }

            TextField(
                value = text,
                onValueChange = {  text = it },
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Words),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                textStyle = TextStyle.Default.copy(fontSize = 24.sp)
            )
            Butt.Next {
                validator(text)?.let(snack) ?: run(next)
            }
        }
    }
}