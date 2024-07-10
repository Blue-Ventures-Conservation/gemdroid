package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.SeparateChart
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import org.blueventures.gemdroid.model.Licenses

object Charts {
    fun prep(chart: AnyChartView, charter: () -> SeparateChart): SeparateChart {
        chart.setLicenceKey(Licenses.anychart)
        return chart(charter)
    }

    fun chart(charter: () -> SeparateChart): SeparateChart {
        val c = charter()
        c.credits().text("")
        return c
    }

    @Composable
    fun BarChart(title: String, xLabel: String, yLabel: String, data: List<DataEntry>, xUnit: String = "", yUnit: String = "", fill: Boolean = true, height: Dp = 350.dp) {
        var modifier = Modifier.fillMaxWidth()
        modifier = if (fill) modifier.fillMaxHeight(0.67f) else modifier.height(height)
        AndroidView(
            modifier = modifier,
            factory = { context ->
                AnyChartView(context).apply {
                    val cartesian = prep(this, AnyChart::column) as Cartesian

                    val column = cartesian.column(data)

                    column.tooltip()
                        .titleFormat("{%X} $xUnit")
                        .position(Position.CENTER_BOTTOM)
                        .anchor(Anchor.CENTER_BOTTOM)
                        .offsetX(0.0)
                        .offsetY(5.0)
                        .format("{%Value}{groupsSeparator: } $yUnit")

                    cartesian.animation(true)
                    cartesian.title(title)

                    cartesian.yScale().minimum(0)

                    cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }")

                    cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                    cartesian.interactivity().hoverMode(HoverMode.BY_X)

                    cartesian.xAxis(0).title(xLabel)
                    cartesian.yAxis(0).title(yLabel)

                    setChart(cartesian)
                }
            }
        )
    }
}