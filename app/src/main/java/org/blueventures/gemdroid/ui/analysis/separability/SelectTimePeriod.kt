package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.theme.SkyBlue

object SelectTimePeriod {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, snack: SnackFun, next: Click, back: Click) {
        appBar(AppBarUpdate(title = "Spectral Separability"))

        val (cras, setCRAs) = remember { mutableStateOf<Result<CRA>?>(null) }

        when {
            cras == null -> {
                PleaseWait()
                viewModel.loadCRAs(setCRAs)
            }
            cras.isFailure -> {
                snack(cras.exceptionOrNull()!!.message!!)
            }
            else -> {
                val cra = cras.getOrNull()!!
                val cont = cra.contemporaryCRA
                val hist = cra.historicalShp()
                val setShp: (Shapefile) -> Unit = { viewModel.toAnalyze = it }
                val setCont = { setShp(cont) }
                val setHist = { setShp(hist) }
                Dashboard(viewModel, { setCont(); next() }, { setCont(); next() }, { setHist(); next() }) { setHist(); next() }
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun Dashboard(viewModel: SeparabilityViewModel, contHigh: Click, contLow: Click, histHigh: Click, histLow: Click) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
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

    @Composable
    fun DashboardButton(label: String, click: Click) {
        Card(
            border = BorderStroke(2.dp, SkyBlue),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { click() },
        ) {
            Text(text = label, fontSize = 20.sp, textAlign = TextAlign.Center)
        }
    }
}