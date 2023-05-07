package org.blueventures.gemdroid.ui.analysis.separability

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
        LocalRemote(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile, { code, message ->
            if (code == 400 && message?.contains("missing asset") == true) {
                Pair("CRA not found on the backend.", false)
            } else {
                Pair(null, true)
            }
        }) { data ->
            Layout(data, back)
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun Layout(data: Map<String, Any>, back: Click) {
        val (bandInfo, setBandInfo) = remember { mutableStateOf<Pair<String, Map<String, List<Double>>>?>(null) }
        val bandsAndClasses = JSONMap.bandsAndClasses(data)

        if (bandsAndClasses == null) {
            LaunchedEffect(key1 = true) {
                back()
            }
            return
        }

        val bands = bandsAndClasses.first
        val classes = bandsAndClasses.second
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = bandInfo?.first ?: "", fontSize = 16.sp)
            Chart(bandInfo?.second, classes)
            Dropdown(title = "Select Band", labels = bands) { i ->
                val band = bands[i]
                setBandInfo(JSONMap.boxChartBandInfo(band, data))
            }
        }
    }

    @Composable
    fun Chart(data: Map<String, List<Double>>?, classes: List<String>) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f),
            update = { layout ->
                if (data == null) {
                    return@AndroidView
                }

                val view = AnyChartView(layout.context)
                val cartesian = Charts.prep(view, AnyChart::box)

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
                view.setChart(cartesian)
                layout.addView(view)
            },
            factory = { context ->
                FrameLayout(context)
            }
        )
    }

    private fun conv(d: Double): Int {
        return (d * 100000).toInt()
    }
}