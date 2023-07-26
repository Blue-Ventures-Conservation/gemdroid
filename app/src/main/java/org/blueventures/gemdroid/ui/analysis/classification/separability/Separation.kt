package org.blueventures.gemdroid.ui.analysis.classification.separability

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.theme.g2R2B

object Separation {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, back: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        GetRemote.Save(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile, CRA::errHandler) { json ->
            Layout(json, back)
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun Layout(json: Map<String, Any>, back: Click) {
        val bandsAndClasses = JSONMap.bandsAndClasses(json)

        if (bandsAndClasses == null) {
            Effect.Once { back() }
            return
        }

        val bands = bandsAndClasses.first
        val classes = bandsAndClasses.second
        Col.Between {
            Chart(json, bands, classes)
        }
    }

    @Composable
    fun Chart(json: Map<String, Any>, bands: List<String>, classes: List<String>) {
        val (data, setData) = remember { mutableStateOf<Pair<String, Map<String, List<Double>>>?>(null) }
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
                val cartesian = Charts.prep(view, AnyChart::box) as Cartesian

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
        Dropdown(title = stringResource(R.string.select_band), labels = bands) { i ->
            val band = bands[i]
            setData(JSONMap.boxChartBandInfo(json, band))
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