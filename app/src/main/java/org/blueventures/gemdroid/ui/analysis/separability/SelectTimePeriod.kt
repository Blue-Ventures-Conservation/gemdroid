package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.Shapefile
import org.blueventures.gemdroid.model.analysis.separability.ContemporaryHighTide
import org.blueventures.gemdroid.model.analysis.separability.ContemporaryLowTide
import org.blueventures.gemdroid.model.analysis.separability.HistoricalHighTide
import org.blueventures.gemdroid.model.analysis.separability.HistoricalLowTide
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.analysis.separability.TimePeriod
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.SnackFun

object SelectTimePeriod {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        appBar(AppBarUpdate(title = "Spectral Separability"))

        val (cras, setCRAs) = remember { mutableStateOf<Result<Pair<CRA, Throwable?>>?>(null) }

        when {
            cras == null -> {
                PleaseWait()
                viewModel.loadCRAs(setCRAs)
            }
            cras.isFailure -> {
                Effect.Once {
                    snack(cras.exceptionOrNull()!!.message!!)
                    back()
                }
            }
            else -> {
                val pair = cras.getOrNull()!!
                if (pair.second != null) {
                    Effect.Once {
                        snack("Could not verify CRA upload, charts may not be available.")
                    }
                }

                val cra = pair.first
                val cont = cra.contemporaryCRA
                val hist = cra.historicalShp()
                val setShp: (Shapefile) -> Unit = { viewModel.toAnalyze = it }
                val setCont = { setShp(cont) }
                val setHist = { setShp(hist) }
                Dashboard(viewModel, { setCont(); next() }, { setCont(); next() }, { setHist(); next() }) { setHist(); next() }
            }
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun Dashboard(viewModel: SeparabilityViewModel, contHigh: Click, contLow: Click, histHigh: Click, histLow: Click) {
        Col.Dash("Select a time period and tidal condition to inspect:") {
            val setPeriod: (TimePeriod) -> Unit = { viewModel.timePeriod = it }
            HighLow(viewModel, "Contemporary", { setPeriod(ContemporaryHighTide); contHigh() }) {
                setPeriod(ContemporaryLowTide)
                contLow()
            }
            HighLow(viewModel, "Historical", { setPeriod(HistoricalHighTide); histHigh() }) {
                setPeriod(HistoricalLowTide)
                histLow()
            }
        }
    }

    @Composable
    fun HighLow(viewModel: SeparabilityViewModel, temporal: String, high: Click, low: Click) {
        val setTitle: (String) -> Unit = { viewModel.title = it }
        val highLabel = "$temporal High Tide"
        val lowLabel = "$temporal Low Tide"
        DashboardButton(highLabel) { setTitle(highLabel); high() }
        DashboardButton(lowLabel) { setTitle(lowLabel); low() }
    }
}