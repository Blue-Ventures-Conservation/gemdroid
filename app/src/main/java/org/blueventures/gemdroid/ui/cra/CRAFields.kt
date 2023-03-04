package org.blueventures.gemdroid.ui.cra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.cra.CraViewModel
import org.blueventures.gemdroid.model.cra.Fields
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object CRAFields {
    @Composable
    fun Screen(viewModel: CraViewModel, snack: SnackFun, done: Click, back: Click) {
        val (saving, setSaving) = remember{ mutableStateOf(false) }

        if (saving) {
            Progress()
        } else {
            CRAFields(viewModel, snack, setSaving, done, back)
        }
    }

    @Composable
    fun CRAFields(viewModel: CraViewModel, snack: SnackFun, setSaving: (Boolean) -> Unit, done: Click, back: Click) {
        val (fields, setFields) = remember { mutableStateOf<Result<Fields>?>(null) }

        when {
            fields == null -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    viewModel.getCRAFields(setFields)
                }
            }
            fields.isFailure -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    snack(fields.exceptionOrNull()!!.message!!)
                    back()
                }
            }
            fields.getOrNull()!!.complete() -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    viewModel.setFields(fields.getOrNull()!!)
                    viewModel.saveCRAs { result ->
                        when {
                            result.isSuccess -> done()
                            else -> {
                                snack(result.exceptionOrNull()!!.message!!)
                                back()
                            }
                        }
                    }
                }
            }
            else -> {
                val lists = fields.getOrNull()!!
                SelectFields(viewModel, lists.numerics!!, lists.strings!!, setSaving, snack, done)
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun SelectFields(viewModel: CraViewModel, numerics: List<String>, strings: List<String>, setSaving: (Boolean) -> Unit, snack: SnackFun, done: Click) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val string = remember { mutableStateOf("") }
            val numeric = remember { mutableStateOf("") }
            Dropdown(title = "Select Character Class Field:", labels = strings) { i ->
                string.value = strings[i]
            }
            Dropdown(title = "Select Numeric Class Field:", labels = numerics) { i ->
                numeric.value = numerics[i]
            }

            Button(
                enabled = numeric.value.isNotEmpty() && string.value.isNotEmpty(),
                onClick = {
                    if (numeric.value == string.value) {
                        snack("These cannot both be the same field")
                    } else {
                        setSaving(true)
                        viewModel.setFields(Fields(chosenNumeric = numeric.value, chosenString = string.value))
                        viewModel.saveCRAs {
                            when {
                                it.isSuccess -> done()
                                else -> {
                                    setSaving(false)
                                    snack(it.exceptionOrNull()!!.message!!)
                                }
                            }
                        }
                    }
                }
            ) {
                Text(text = "Done", fontSize = 16.sp)
            }
        }
    }
}