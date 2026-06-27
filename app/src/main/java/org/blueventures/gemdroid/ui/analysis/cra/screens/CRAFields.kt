package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.cra.BothFieldsCounted
import org.blueventures.gemdroid.data.analysis.cra.ClassCounts
import org.blueventures.gemdroid.data.analysis.cra.Fields
import org.blueventures.gemdroid.data.analysis.cra.StringsNumerics
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Once
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object CRAFields {
    @Composable
    fun Screen(viewModel: CRAViewModel, snack: SnackFun, back: Click, done: Click) {
        val (saving, setSaving) = remember{ mutableStateOf(false) }

        if (saving) {
            PleaseWait()
        } else {
            CRAFields(viewModel, snack, setSaving, back, done)
        }
    }

    @Composable
    fun CRAFields(viewModel: CRAViewModel, snack: SnackFun, setSaving: (Boolean) -> Unit, back: Click, done: Click) {
        val (bothFields, setBothFields) = remember { mutableStateOf<Result<BothFieldsCounted>?>(null) }

        when {
            bothFields == null -> {
                Progress()
                Effect.Once {
                    viewModel.getCRAFields(setBothFields)
                }
            }
            bothFields.isFailure -> {
                Progress()
                val msg = bothFields.exceptionOrNull()!!.localized(LocalContext.current)
                snack.Once(msg)
                done()
            }
            else -> {
                val both = bothFields.getOrNull()!!
                if (both.complete()) {
                    PleaseWait()
                    val ctx = LocalContext.current.applicationContext
                    Effect.Once {
                        var gaveUp = false

                        // fields are complete, so it is only possible for one
                        // or zero of the CRAs to have been parsed locally
                        // if both were parsed locally, we wouldn't know the fields yet
                        // if one is remote and one is parsed locally, we are using the fields from the remote
                        if (both.histCounts.parsedLocally()) {
                            val hp = viewModel.handleLocalCounts(both.histCounts, both.fields.chosenString!!, both.fields.chosenStringValues!!, both.fields.chosenNumeric!!, both.histCounts.numericCounts.getChosenNumerics(both.fields.chosenNumeric))
                            if (hp.second != null) {
                                gaveUp = true
                                snack(ctx.getString(hp.second!!).format(ctx.getString(R.string.historical)))
                                done()
                            } else {
                                both.histCounts.stringCounts = ClassCounts(chosenCounts = hp.first)
                            }
                        } else if (both.contCounts.parsedLocally()) {
                            val cp = viewModel.handleLocalCounts(both.contCounts, both.fields.chosenString!!, both.fields.chosenStringValues!!, both.fields.chosenNumeric!!, both.contCounts.numericCounts.getChosenNumerics(both.fields.chosenNumeric))
                            if (cp.second != null) {
                                gaveUp = true
                                snack(ctx.getString(cp.second!!).format(ctx.getString(R.string.contemporary)))
                                done()
                            } else {
                                both.contCounts.stringCounts = ClassCounts(chosenCounts = cp.first)
                            }
                        }

                        if (!gaveUp) {
                            val chosenHistCounts = both.histCounts.stringCounts.chosenCounts
                            val chosenContCounts = both.contCounts.stringCounts.chosenCounts
                            if (chosenHistCounts.isNotEmpty() && chosenContCounts.isNotEmpty()) {
                                viewModel.compareCRAs(chosenHistCounts, chosenContCounts)?.let {
                                    gaveUp = true
                                    snack(ctx.getString(it))
                                    done()
                                }
                            }

                            if (!gaveUp) {
                                viewModel.setFields(both)
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
                    }
                } else {
                    SelectFields(viewModel, both, setSaving, snack, back, done)
                }
            }
        }
    }

    @Composable
    fun SelectFields(viewModel: CRAViewModel, both: BothFieldsCounted, setSaving: (Boolean) -> Unit, snack: SnackFun, back: Click, done: Click) {
        Col.Col {
            val strings = both.fields.strings!!
            val numerics = both.fields.numerics!!
            val stringValues = both.fields.stringValues!!
            val numericValues = both.fields.numericValues!!

            var chosenString by remember { mutableStateOf("") }
            var chosenNumeric by remember { mutableStateOf("") }
            Dropdown(title = stringResource(R.string.select_char_field), labels = strings) { i ->
                chosenString = strings[i]
            }
            Dropdown(title = stringResource(R.string.select_num_field), labels = numerics) { i ->
                chosenNumeric = numerics[i]
            }

            val ctx = LocalContext.current.applicationContext
            Butt.Done(chosenNumeric.isNotEmpty() && chosenString.isNotEmpty()) {
                val chosenStrings = stringValues[chosenString]!!
                val chosenNumerics = numericValues[chosenNumeric]!!
                if (chosenNumeric == chosenString) {
                    snack(ctx.getString(R.string.cannot_be_same_field))
                } else if (chosenStrings.size != chosenNumerics.size) {
                    snack(ctx.getString(R.string.no_matching_numbers_of_classes))
                } else {
                    val histCountsPair = viewModel.handleLocalCounts(both.histCounts, chosenString, chosenStrings, chosenNumeric, chosenNumerics)
                    histCountsPair.second?.let {
                        snack(ctx.getString(it).format(ctx.getString(R.string.historical)))
                        done()
                    } ?: run {
                        val contsCountsPair = viewModel.handleLocalCounts(both.contCounts, chosenString, chosenStrings, chosenNumeric, chosenNumerics)
                        contsCountsPair.second?.let {
                            snack(ctx.getString(it).format(ctx.getString(R.string.contemporary)))
                            done()
                        } ?: run {
                            val chosenHistCounts = histCountsPair.first
                            val chosenContCounts = contsCountsPair.first

                            viewModel.compareCRAs(chosenHistCounts, chosenContCounts)?.let {
                                snack(ctx.getString(it))
                                done()
                            } ?: run {
                                setSaving(true)
                                val zipped = chosenNumerics.zip(chosenStrings)
                                viewModel.setFields(BothFieldsCounted(
                                    Fields(chosenNumeric = chosenNumeric, chosenString = chosenString, chosenStringValues = zipped.sortedBy { it.first }.map { it.second }),
                                    StringsNumerics(ClassCounts(chosenCounts = chosenHistCounts)),
                                    StringsNumerics(ClassCounts(chosenCounts = chosenContCounts)),
                                ))
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
        }
    }
}