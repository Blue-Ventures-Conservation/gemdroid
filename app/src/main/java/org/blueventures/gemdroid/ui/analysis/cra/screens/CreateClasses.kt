package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.CRAClass
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.minCRAClasses
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.ClickContent
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.FloatingButtons
import java.util.UUID

object CreateClasses {
    class TextInput(val id: String = UUID.randomUUID().toString(), var value: String = "")
    @Composable
    fun Screen(viewModel: CRAViewModel, next: Click) {
        Col.Col {
            Text(text = stringResource(R.string.name_your_classes), fontSize = 24.sp, textAlign = TextAlign.Center)
            Box(modifier = Modifier.fillMaxSize()) {
                val placeholder = stringResource(R.string.please_provide_a_name)
                val inputs = remember { mutableStateListOf(TextInput(), TextInput()) }
                val (showDoneDialog, setShowDoneDialog) = remember { mutableStateOf<Unit?>(null) }

                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        items(inputs, { it.id }) { input ->
                            ClassRow(placeholder, input)
                        }
                    }

                    showDoneDialog?.let {
                        DoneDialog(viewModel, inputs.toList(), next) { setShowDoneDialog(null) }
                    }
                }
                FloatingButtons(ClickContent({
                    inputs.add(TextInput())
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_another_land_cover_class))
                }, ClickContent({
                    setShowDoneDialog(Unit)
                }) {
                    Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.done_creating_land_cover_classes))
                })
            }
        }
    }

    @Composable
    fun ClassRow(placeholder: String, input: TextInput) {
        val focus = LocalFocusManager.current
        var text by remember { mutableStateOf(input.value) }
        TextField(
            value = text,
            onValueChange = {
                input.value = it
                text = it
            },
            modifier = Modifier.padding(24.dp),
            label = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Words),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            textStyle = TextStyle.Default.copy(fontSize = 24.sp)
        )
    }

    @Composable
    fun DoneDialog(viewModel: CRAViewModel, inputs: List<TextInput>, done: Click, onDismiss: Click) {
        val validClasses = mutableListOf<CRAClass>()
        inputs.forEachIndexed { index, input ->
            val name = input.value.trim()
            if (name.isNotEmpty()) {
                validClasses.add(CRAClass(index + 1, name))
            }
        }

        val valid = validClasses.size >= minCRAClasses

        AlertDialog(
            onDismissRequest = onDismiss,
            text = { Text(text = if (valid) stringResource(R.string.all_done_creating_your_land_cover_classes) else stringResource(R.string.please_create_at_least_2_classes)) },
            confirmButton = {
                val context = LocalContext.current
                Butt.Text(if (valid) stringResource(R.string.done_button) else stringResource(android.R.string.ok)) {
                    if (valid) {
                        viewModel.setCRAClasses(context, validClasses)
                        done()
                    } else {
                        onDismiss()
                    }
                }
            },
            dismissButton = {
                Butt.Text(stringResource(R.string.cancel), click = onDismiss)
            }
        )
    }
}