package org.blueventures.gemdroid.ui.analysis.separability

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import org.blueventures.gemdroid.data.JSONMap
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.separability.Separability.Layout
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.LocalRemote
import org.blueventures.gemdroid.ui.theme.g2R2B

object Separation {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, back: Click) {
        LocalRemote(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile, Separability::craErrorHandler) { json ->
            Layout(json, back) { data, bands, classes, setData ->
                Chart(json, data, bands, classes, setData)
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun Chart(json: Map<String, Any>, data: Pair<String, Map<String, List<Double>>>?, bands: List<String>, classes: List<String>, setData: (Pair<String, Map<String, List<Double>>>?) -> Unit) {
        Text(text = data?.first ?: "", fontSize = 16.sp)
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

                val size = classes.size
                val entries = mutableListOf<DataEntry>()
                for ((idx, cls) in classes.withIndex()) {
                    val cdat = data.second[cls] ?: continue
                    if (cdat.size < 5) continue
                    entries.add(BoxDataEntry(cls, g2R2B(idx, size), cdat[0], cdat[1], cdat[2], cdat[3], cdat[4]))
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
        Dropdown(title = "Select Band", labels = bands) { i ->
            val band = bands[i]
            setData(JSONMap.boxChartBandInfo(band, json))
        }
    }

    /**
     * We have our own BoxDataEntry (rather than using com.anychart.chart.common.dataentry.BoxDataEntry)
     * Because we want to use Doubles, not Integers.
     */
    class BoxDataEntry(x: String, color: String, low: Number, q1: Number, median: Number, q3: Number, high: Number) : DataEntry() {
        init {
            setValue("x", x)
            setValue("fill", color)
            setValue("low", low)
            setValue("q1", q1)
            setValue("median", median)
            setValue("q3", q3)
            setValue("high", high)
        }
    }
}