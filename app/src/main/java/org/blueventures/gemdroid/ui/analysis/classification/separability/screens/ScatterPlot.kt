package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
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
import org.blueventures.gemdroid.data.PointD
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.theme.g2R2BHex

object ScatterPlot {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, back: Click) {
        Nav.Wrap(back) { nav ->
            appBar(AppBarUpdate(viewModel.title))

            GetRemote.Display(viewModel::loadScatterFile, viewModel::getScatter, errorHandler = CRA::errHandler) { json ->
                val classes = JSONMap.classes(json)

                if (classes == null) {
                    Effect.Once { nav.back() }
                    return@Display
                }

                val data = JSONMap.scatterChartInfo(json, classes, viewModel.bandX, viewModel.bandY)

                if (data == null) {
                    Effect.Once { nav.back() }
                    return@Display
                }

                Chart(data, classes, viewModel.bandX, viewModel.bandY)
            }
        }
    }

    @Composable
    fun Chart(data: Map<String, List<PointD>>, classes: List<String>, bandX: String, bandY: String) {
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
                        entries.add(ScatterDataEntry(cls, color, pt.x, pt.y))
                    }
                    idx++
                }

                val view = AnyChartView(layout.context)
                val scatter = Charts.prep(view, AnyChart::scatter) as Scatter
                scatter.xAxis(0).title(bandX)
                scatter.yAxis(0).title(bandY)
                val marker = scatter.marker(entries)
                marker.type(MarkerType.CIRCLE).size(14)
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

    class ScatterDataEntry(cls: String, color: String, x: Number, y: Number) : DataEntry() {
        init {
            setValue("title", cls)
            setValue("fill", color)
            setValue("stroke", color)
            setValue("x", x)
            setValue("value", y)
        }
    }
}