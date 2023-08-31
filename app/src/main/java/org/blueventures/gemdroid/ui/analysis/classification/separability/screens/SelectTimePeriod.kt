package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.cra.Shapefile
import org.blueventures.gemdroid.model.analysis.classification.separability.ContemporaryHighTide
import org.blueventures.gemdroid.model.analysis.classification.separability.ContemporaryLowTide
import org.blueventures.gemdroid.model.analysis.classification.separability.HistoricalHighTide
import org.blueventures.gemdroid.model.analysis.classification.separability.HistoricalLowTide
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.analysis.classification.separability.TimePeriod
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun

object SelectTimePeriod {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        Nav.Wrap(back, next) { nav ->
            appBar(AppBarUpdate(stringResource(R.string.spectral_separability)))

            Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras_charts), viewModel.craAwaiter) { cra ->
                val cont = cra.contemporaryCRA
                val hist = cra.historicalShp()
                val setShp: (Shapefile) -> Unit = { viewModel.toAnalyze = it }
                val setCont = { setShp(cont) }
                val setHist = { setShp(hist) }
                Dashboard(viewModel, { setCont(); nav.next() }, { setCont(); nav.next() }, { setHist(); nav.next() }) { setHist(); nav.next() }
            }
        }
    }

    @Composable
    fun Dashboard(viewModel: SeparabilityViewModel, contHigh: Click, contLow: Click, histHigh: Click, histLow: Click) {
        Col.Dash(stringResource(R.string.select_time_period)) {
            val setPeriod: (TimePeriod) -> Unit = { viewModel.timePeriod = it }
            HighLow(viewModel, stringResource(R.string.contemporary), { setPeriod(ContemporaryHighTide); contHigh() }) {
                setPeriod(ContemporaryLowTide)
                contLow()
            }
            HighLow(viewModel, stringResource(R.string.historical), { setPeriod(HistoricalHighTide); histHigh() }) {
                setPeriod(HistoricalLowTide)
                histLow()
            }
        }
    }

    @Composable
    fun HighLow(viewModel: SeparabilityViewModel, temporal: String, high: Click, low: Click) {
        val setTitle: (String) -> Unit = { viewModel.title = it }
        val highLabel = "$temporal ${stringResource(R.string.high_tide)}"
        val lowLabel = "$temporal ${stringResource(R.string.low_tide)}"
        DashboardButton(highLabel) { setTitle(highLabel); high() }
        DashboardButton(lowLabel) { setTitle(lowLabel); low() }
    }
}