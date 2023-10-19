package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.Buffers
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object Buffer {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun, back: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(stringResource(R.string.roi_buffer_title)))

            val (saving, setSaving) = remember { mutableStateOf(false) }

            if (saving) {
                Progress()
            } else {
                GetRemote.Save(viewModel::loadBuffersFile, viewModel::getBuffers, viewModel::saveBuffersFile) { buffs ->
                    BufferChoice(viewModel, buffs, snack, back, setSaving)
                }
            }
        }
    }

    @Composable
    fun BufferChoice(viewModel: AnalysisViewModel, buffers: Buffers, snack: SnackFun, back: Click, saving: (Boolean) -> Unit) {
        val (bufferDist, setBufferDist) = remember { mutableStateOf(Pair(-1, false)) }

        Col.Col {
            Chart(buffers)
            Dropdown(title = stringResource(R.string.select_buffer_distance), labels = buffers.buffers.keys) { i ->
                setBufferDist(Pair(buffers.buffers.vals[i], true))
            }
            val ctx = LocalContext.current
            Butt.Done(bufferDist.second) {
                saving(true)
                viewModel.saveBuffer(bufferDist.first) { result ->
                    saving(false)
                    if (result.isSuccess) {
                        back()
                    } else {
                        snack(result.exceptionOrNull()!!.localized(ctx))
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
                    val cartesian = Charts.prep(this, AnyChart::column) as Cartesian

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