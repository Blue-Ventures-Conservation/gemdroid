package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.HeatMap
import com.anychart.enums.SelectionMode
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.theme.Caution
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.LightGrey
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.toHexString

object Correlation {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, back: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        GetRemote.Save(viewModel::loadCorrelationFile, viewModel::getCorrelation, viewModel::saveCorrelationFile, CRA::errHandler) { json ->
            val corrs = JSONMap.correlationChartInfo(json)

            if (corrs == null) {
                Effect.Once { back() }
                return@Save
            }

            Chart(corrs)
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun Chart(correlations: JSONMap.Correlations) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                update = { layout ->
                    val view = AnyChartView(layout.context)
                    val heat = Charts.prep(view, AnyChart::heatMap) as HeatMap


                    heat.stroke("1 #fff")
                    heat.hovered()
                        .stroke("2 #fff")
                        .labels("{ fontColor: '#fff' }")

                    heat.interactivity().selectionMode(SelectionMode.NONE)

                    heat.title().enabled(true)
                    heat.title()
                        .text(layout.context.getString(R.string.correlation_matrix))
                        .padding(0.0, 0.0, 20.0, 0.0)

                    heat.labels().enabled(true)
                    heat.labels()
                        .format("{%title}")

                    heat.yAxis(0).stroke(null)
                    heat.yAxis(0).labels().padding(0.0, 15.0, 0.0, 0.0)
                    heat.yAxis(0).ticks(false)
                    heat.xAxis(0).stroke(null)
                    heat.xAxis(0).ticks(false)

                    heat.tooltip().title().useHtml(true)
                    heat.tooltip()
                        .useHtml(true)
                        .titleFormat("{%title}")
                        .format("{%x} • {%y}:  {%heat}")

                    val entries = mutableListOf<DataEntry>()
                    for (x in correlations.bands) {
                        val row = correlations.row(x)

                        for ((index, cell) in row.withIndex()) {
                            val y = correlations.bands[index]
                            entries.add(HeatDataEntry(cellTitle(x, y, cell.correlation), cellColor(x, y, cell.correlation), x, y, cell.value))
                        }
                    }

                    heat.data(entries)

                    view.setChart(heat)
                    layout.addView(view)
                },
                factory = { context ->
                    FrameLayout(context)
                }
            )
        }
    }

    private fun cellTitle(x: String, y: String, correlation: JSONMap.Correlation): String {
        if (x == y) return "x"
        return correlation.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun cellColor(x: String, y: String, correlation: JSONMap.Correlation): String {
        if (x == y) return LightGrey.toHexString()
        return when(correlation) {
            JSONMap.Correlation.NONE -> LightGreen.toHexString()
            JSONMap.Correlation.MODERATE -> Caution.toHexString()
            JSONMap.Correlation.HIGH -> MildRed.toHexString()
        }
    }

    class HeatDataEntry(correlation: String, color: String, x: String, y: String, heat: Number) : DataEntry() {
        init {
            setValue("title", correlation)
            setValue("fill", color)
            setValue("stroke", color)
            setValue("x", x)
            setValue("y", y)
            setValue("heat", heat)
        }
    }
}