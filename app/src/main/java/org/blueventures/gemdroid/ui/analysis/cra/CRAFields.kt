package org.blueventures.gemdroid.ui.analysis.cra

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.blueventures.gemdroid.model.analysis.cra.CraViewModel
import org.blueventures.gemdroid.model.analysis.cra.Fields
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object CRAFields {
    @Composable
    fun Screen(viewModel: CraViewModel, snack: SnackFun, done: Click, back: Click) {
        val (saving, setSaving) = remember{ mutableStateOf(false) }

        if (saving) {
            PleaseWait()
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
                Effect.Once {
                    viewModel.getCRAFields(setFields)
                }
            }
            fields.isFailure -> {
                Progress()
                Effect.Once {
                    snack(fields.exceptionOrNull()!!.message!!)
                    back()
                }
            }
            fields.getOrNull()!!.complete() -> {
                PleaseWait()
                Effect.Once {
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
                SelectFields(viewModel, fields.getOrNull()!!, setSaving, snack, done, back)
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun SelectFields(viewModel: CraViewModel, lists: Fields, setSaving: (Boolean) -> Unit, snack: SnackFun, done: Click, back: Click) {
        Col.Between {
            val strings = lists.strings!!
            val numerics = lists.numerics!!
            val stringValues = lists.stringValues!!
            val string = remember { mutableStateOf("") }
            val numeric = remember { mutableStateOf("") }
            Dropdown(title = "Select Character Class Field:", labels = strings) { i ->
                string.value = strings[i]
            }
            Dropdown(title = "Select Numeric Class Field:", labels = numerics) { i ->
                numeric.value = numerics[i]
            }

            Butt.Done(numeric.value.isNotEmpty() && string.value.isNotEmpty()) {
                if (numeric.value == string.value) {
                    snack("These cannot both be the same field")
                } else {
                    setSaving(true)
                    viewModel.setFields(Fields(chosenNumeric = numeric.value, chosenString = string.value, chosenStringValues = stringValues[string.value]))
                    viewModel.saveCRAs {
                        when {
                            it.isSuccess -> done()
                            else -> {
                                snack(it.exceptionOrNull()!!.message!!)
                                back()
                            }
                        }
                    }
                }
            }
        }
    }
}