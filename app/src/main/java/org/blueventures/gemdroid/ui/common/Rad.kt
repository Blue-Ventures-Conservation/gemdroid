package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

object Rad {
    /**
     * Radio button group
     */
    @Composable
    fun <T> Io(choices: List<T>, default: T?, textGetter: (T) -> Int, onClick: (T) -> Unit) {
        val (choice, setChoice) = remember { mutableStateOf(default) }
        InnerIo(choices, choice, setChoice, textGetter)
        Spacer(modifier = Modifier.height(0.dp))
        Butt.Next(choice != null) { choice?.let { onClick(it) } }
    }

    /**
     * Provided for when the button exists in a different composable.
     */
    @Composable
    fun <T> InnerIo(choices: List<T>, choice: T?, setChoice: (T?) -> Unit, textGetter: (T) -> Int) {
        choices.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option?.equals(choice) == true),
                        onClick = { setChoice(option) }
                    )
            ) {
                RadioButton(selected = (option?.equals(choice) == true), onClick = { setChoice(option) })
                Text(text = stringResource(textGetter(option)), modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}