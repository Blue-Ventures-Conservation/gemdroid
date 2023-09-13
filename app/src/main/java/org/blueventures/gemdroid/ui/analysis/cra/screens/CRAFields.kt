package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.cra.Fields
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object CRAFields {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBarFun, snack: SnackFun, back: Click, done: Click) {
        Nav.Wrap(back, done) { nav ->
            appBar(AppBarUpdate(stringResource(R.string.classification_reference_areas)))
            val (saving, setSaving) = remember{ mutableStateOf(false) }

            if (saving) {
                PleaseWait()
            } else {
                CRAFields(viewModel, snack, setSaving, nav::back, nav::next)
            }
        }
    }

    @Composable
    fun CRAFields(viewModel: CRAViewModel, snack: SnackFun, setSaving: (Boolean) -> Unit, back: Click, done: Click) {
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
                val msg = fields.exceptionOrNull()!!.localized(LocalContext.current)
                Effect.Once {
                    snack(msg)
                    back()
                }
            }
            fields.getOrNull()!!.complete() -> {
                PleaseWait()
                val ctx = LocalContext.current
                Effect.Once {
                    viewModel.setFields(fields.getOrNull()!!)
                    viewModel.saveCRAs { result ->
                        when {
                            result.isSuccess -> done()
                            else -> {
                                snack(result.exceptionOrNull()!!.localized(ctx))
                                back()
                            }
                        }
                    }
                }
            }
            else -> {
                SelectFields(viewModel, fields.getOrNull()!!, setSaving, snack, back, done)
            }
        }
    }

    @Composable
    fun SelectFields(viewModel: CRAViewModel, lists: Fields, setSaving: (Boolean) -> Unit, snack: SnackFun, back: Click, done: Click) {
        Col.Col {
            val strings = lists.strings!!
            val numerics = lists.numerics!!
            val stringValues = lists.stringValues!!
            val string = remember { mutableStateOf("") }
            val numeric = remember { mutableStateOf("") }
            Dropdown(title = stringResource(R.string.select_char_field), labels = strings) { i ->
                string.value = strings[i]
            }
            Dropdown(title = stringResource(R.string.select_num_field), labels = numerics) { i ->
                numeric.value = numerics[i]
            }

            val ctx = LocalContext.current
            Butt.Done(numeric.value.isNotEmpty() && string.value.isNotEmpty()) {
                if (numeric.value == string.value) {
                    snack(ctx.getString(R.string.cannot_be_same_field))
                } else {
                    setSaving(true)
                    viewModel.setFields(Fields(chosenNumeric = numeric.value, chosenString = string.value, chosenStringValues = stringValues[string.value]))
                    viewModel.saveCRAs {
                        when {
                            it.isSuccess -> done()
                            else -> {
                                snack(it.exceptionOrNull()!!.localized(ctx))
                                back()
                            }
                        }
                    }
                }
            }
        }
    }
}