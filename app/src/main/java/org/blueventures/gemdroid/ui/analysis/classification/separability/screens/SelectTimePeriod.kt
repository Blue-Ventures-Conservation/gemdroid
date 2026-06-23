package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.shp.RemoteCRAFileInfo
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.ContemporaryHighTide
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.ContemporaryLowTide
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.HistoricalHighTide
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.HistoricalLowTide
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.TimePeriod
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.SnackFun

object SelectTimePeriod {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBar, snack: SnackFun, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.spectral_separability)))

        Await.CRAOrGoBack(snack, next, stringResource(R.string.could_not_verify_cras_charts), viewModel.craAwaiter) { cra ->
            val cont = cra.contemporaryCRA
            val hist = cra.historicalShp()
            val setCRAInfo: (RemoteCRAFileInfo) -> Unit = { viewModel.toAnalyze = it }
            val setCont = { setCRAInfo(cont) }
            val setHist = { setCRAInfo(hist) }
            Dashboard(viewModel, { setCont(); next() }, { setCont(); next() }, { setHist(); next() }) { setHist(); next() }
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