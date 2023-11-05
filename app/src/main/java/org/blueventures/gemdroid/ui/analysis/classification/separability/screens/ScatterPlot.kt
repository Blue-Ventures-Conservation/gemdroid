package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Scatter
import com.anychart.enums.MarkerType
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.theme.g2R2BHex

object ScatterPlot {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBar, back: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.title))

            val screen: @Composable (Map<String, Any>) -> Unit = { json ->
                val data = JSONMap.scatterChartInfo(json, viewModel.classes, viewModel.bandX, viewModel.bandY)

                if (data == null) {
                    Effect.Once { back() }
                } else {
                    Chart(data, viewModel.classes, viewModel.bandX, viewModel.bandY)
                }
            }

            GetRemote.Display(viewModel::loadScatterFile, viewModel::getScatter, errorHandler = CRA::errHandler, setLocal = screen, setRemote = screen)
        }
    }

    @Composable
    fun Chart(data: Map<String, List<JSONMap.ScatterPoint>>, classes: List<String>, bandX: String, bandY: String) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            update = { layout ->
                var idx = 0
                val size = classes.size
                val entries = mutableListOf<DataEntry>()
                for ((cls, pts) in data) {
                    val color = g2R2BHex(idx, size)
                    for (pt in pts) {
                        entries.add(ScatterDataEntry(cls, color, pt.id, pt.x, pt.y))
                    }
                    idx++
                }

                val view = AnyChartView(layout.context)
                val scatter = Charts.prep(view, AnyChart::scatter) as Scatter
                scatter.xAxis(0).title(bandX)
                scatter.yAxis(0).title(bandY)
                val marker = scatter.marker(entries)
                marker.type(MarkerType.CIRCLE).size(6)
                marker.tooltip().titleFormat("{%title}")
                marker.tooltip().format("$bandX: {%x}\\n$bandY: {%value}")

                view.setChart(scatter)
                layout.addView(view)
            },
            factory = { context ->
                FrameLayout(context)
            }
        )
    }

    class ScatterDataEntry(cls: String, color: String, id: Number?, x: Number, y: Number) : DataEntry() {
        init {
            var title = cls
            if (id != null) {
                title += " (${id.toInt()})"
            }
            setValue("title", title)
            setValue("fill", color)
            setValue("stroke", "#000000")
            setValue("x", x)
            setValue("value", y)
        }
    }
}