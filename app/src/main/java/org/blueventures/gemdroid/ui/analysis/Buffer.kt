package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.LocalRemote
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object Buffer {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun, back: Click) {
        appBar(AppBarUpdate(title = "ROI Buffer"))

        val (saving, setSaving) = remember { mutableStateOf(false) }

        if (saving) {
            Progress()
        } else {
            LocalRemote(viewModel::loadBuffersFile, viewModel::getBuffers, viewModel::saveBuffersFile) { buffs ->
                BufferChoice(viewModel, buffs, snack, back, setSaving)
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun BufferChoice(viewModel: AnalysisViewModel, buffers: Buffers, snack: SnackFun, back: Click, saving: (Boolean) -> Unit) {
        val (bufferDist, setBufferDist) = remember { mutableStateOf(Pair(-1, false)) }

        Col.Between {
            Chart(buffers)
            Dropdown(title = "Select buffer distance:", labels = buffers.buffers.keys) { i ->
                setBufferDist(Pair(buffers.buffers.vals[i], true))
            }
            Butt.Done(bufferDist.second) {
                saving(true)
                viewModel.saveBuffer(bufferDist.first) { result ->
                    saving(false)
                    if (result.isSuccess) {
                        back()
                    } else {
                        snack(result.exceptionOrNull()!!.message!!)
                    }
                }
            }
        }
    }

    @Composable
    fun Chart(buffers: Buffers) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f),
            factory = { context ->
                AnyChartView(context).apply {
                    val cartesian = Charts.prep(this, AnyChart::column)

                    val sums = buffers.sums
                    val data = mutableListOf<DataEntry>()
                    sums.keys.forEachIndexed { i, key ->
                        data.add(ValueDataEntry(key, sums.vals[i]))
                    }

                    val column = cartesian.column(data)

                    column.tooltip()
                        .titleFormat("{%X}")
                        .position(Position.CENTER_BOTTOM)
                        .anchor(Anchor.CENTER_BOTTOM)
                        .offsetX(0.0)
                        .offsetY(5.0)
                        .format("{%Value}{groupsSeparator: }")

                    cartesian.animation(true)
                    cartesian.title("Mangrove Area by Buffer")

                    cartesian.yScale().minimum(0)

                    cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }")

                    cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                    cartesian.interactivity().hoverMode(HoverMode.BY_X)

                    cartesian.xAxis(0).title("Shoreline Buffer (km)")
                    cartesian.yAxis(0).title("Area (m²)")

                    setChart(cartesian)
                }
            }
        )
    }
}