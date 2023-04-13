package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.BoxDataEntry
import com.anychart.chart.common.dataentry.DataEntry
import org.blueventures.gemdroid.data.JSONMap
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.LocalRemote

object Separation {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, back: Click) {
        LocalRemote(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile) { data ->
            Layout(data, back)
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun Layout(data: JSONMap, back: Click) {
        val (bandInfo, setBandInfo) = remember { mutableStateOf<Pair<String, Map<String, List<Double>>>?>(null) }
        val bandsAndClasses = data.bandsAndClasses()

        bandsAndClasses?.let {
            val bands = bandsAndClasses.first
            val classes = bandsAndClasses.second
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp)
            ) {
                Text(text = bandInfo?.first ?: "", fontSize = 16.sp)
                Chart(bandInfo?.second, classes)
                Dropdown(title = "Select Band", labels = bands) { i ->
                    val band = bands[i]
                    setBandInfo(data.boxChartBandInfo(band))
                }
            }
        } ?: run {
            back()
        }
    }

    @Composable
    fun Chart(data: Map<String, List<Double>>?, classes: List<String>) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f),
            factory = { context ->
                AnyChartView(context).apply {
                    if (data == null) {
                        return@apply
                    }

                    val cartesian = Charts.prep(this, AnyChart::box)
                    cartesian.title("")
                    cartesian.xAxis(0).staggerMode(true)

                    val entries = mutableListOf<DataEntry>()
                    for (cls in classes) {
                        val cdat = data[cls] ?: continue
                        if (cdat.size < 5) continue
                        entries.add(BoxDataEntry(cls, conv(cdat[0]), conv(cdat[1]), conv(cdat[2]), conv(cdat[3]), conv(cdat[4])))
                    }

                    val box = cartesian.box(entries)
                    box.whiskerWidth("20%")
                    setChart(cartesian)
                }
            }
        )
    }

    private fun conv(d: Double): Int {
        return (d * 100000).toInt()
    }
}