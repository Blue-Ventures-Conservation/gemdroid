package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Cartesian
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.once
import org.blueventures.gemdroid.ui.theme.makeColorPalette
import org.blueventures.gemdroid.ui.theme.toHexString

object Separation {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBar, back: Click) {
        appBar.Update(AppBarUpdate(viewModel.title))

        GetRemote.Save(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile, errorHandler = CRA::errHandler) { json ->
            Layout(json, back)
        }
    }

    @Composable
    fun Layout(json: Map<String, Any>, back: Click) {
        val bandsAndClasses = JSONMap.bandsAndClasses(json)

        if (bandsAndClasses == null) {
            back.once()
            return
        }

        val bands = bandsAndClasses.first
        val classes = bandsAndClasses.second
        Col.Col(scroll = true) {
            Chart(json, bands, classes)
        }
    }

    @Composable
    fun Chart(json: Map<String, Any>, bands: List<String>, classes: List<String>) {
        val (data, setData) = remember { mutableStateOf<Pair<String, Map<String, List<Double>>>?>(null) }
        Text(text = data?.first ?: "", fontSize = 16.sp)

        if (data != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                update = { layout ->
                    val view = AnyChartView(layout.context)
                    val cartesian = Charts.prep(view, AnyChart::box) as Cartesian

                    cartesian.title("")
                    cartesian.xAxis(0).staggerMode(true)

                    val entries = mutableListOf<DataEntry>()
                    val pal = makeColorPalette(classes)
                    for ((idx, cls) in classes.withIndex()) {
                        val cdat = data.second[cls] ?: continue
                        if (cdat.size < 5) continue
                        entries.add(BoxDataEntry(cls, pal[idx].toHexString(), cdat[0], cdat[1], cdat[2], cdat[3], cdat[4]))
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
        val ctx = LocalContext.current
        Dropdown(title = stringResource(R.string.select_band), labels = bands) { i ->
            val band = bands[i]
            setData(JSONMap.boxChartBandInfo(ctx, json, band, classes))
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