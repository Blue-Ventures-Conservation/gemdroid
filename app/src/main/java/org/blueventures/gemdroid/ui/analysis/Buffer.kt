package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.RefreshableError
import org.blueventures.gemdroid.ui.common.SnackFun

object Buffer {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun, back: Click) {
        appBar(AppBarUpdate(title = "ROI Buffer"))

        val (saving, setSaving) = remember { mutableStateOf(false) }

        if (saving) {
            Progress()
        } else {
            Buffer(viewModel, snack, back, setSaving)
        }
    }

    @Composable
    fun Buffer(viewModel: AnalysisViewModel, snack: SnackFun, back: Click, saving: (Boolean) -> Unit) {
        val state by viewModel.state.collectAsState()

        when {
            state.buffers == null -> {
                Progress()
                viewModel.loadBuffersFile()
            }
            !Buffers.isEmpty(state.buffers!!) -> {
                BufferChoice(viewModel, state.buffers!!, snack, back, saving)
            }
            state.buffersResult == null -> {
                PleaseWait()
                LaunchedEffect(key1 = true) {
                    state.roi!!.getOrNull()?.let {
                        viewModel.getBuffers(it)
                    }
                }
            }
            state.buffersResult is ApiResult.Error -> {
                RefreshableError(state.roi!!.getOrNull()!!) { roi, callback ->
                    viewModel.getBuffers(roi, callback)
                }
            }
            state.buffersResult is ApiResult.Success -> {
                val buffers = state.buffersResult!!.data!!
                viewModel.saveBuffersFile(buffers)
                BufferChoice(viewModel, buffers, snack, back, saving)
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun BufferChoice(viewModel: AnalysisViewModel, buffers: Buffers, snack: SnackFun, back: Click, saving: (Boolean) -> Unit) {
        val (doneEnabled, setDoneEnabled) = remember { mutableStateOf(false) }
        val (bufferDist, setBufferDist) = remember { mutableStateOf(-1) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Chart(buffers)
            Dropdown(title = "Select buffer distance:", labels = buffers.buffers.keys) { i ->
                setBufferDist(buffers.buffers.vals[i])
                setDoneEnabled(true)
            }
            Button(
                enabled = doneEnabled,
                onClick = {
                    saving(true)
                    viewModel.saveBuffer(bufferDist) { success ->
                        saving(false)
                        if (success) {
                            viewModel.clearStage()
                            back()
                        } else {
                            snack("Could not save buffer selection!")
                        }
                    }
                },
            ) {
                Text(text = "Done", fontSize = 20.sp)
            }
        }
    }

    @Composable
    fun Chart(buffers: Buffers) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.67f),
                factory = { context ->
                    AnyChartView(context).apply {
                        val chart = this

                        val cartesian = AnyChart.column()
                        val sums = buffers.sums
                        val data = arrayListOf<DataEntry>()
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

                        chart.setChart(cartesian)
                    }
                }
            )
        }
    }
}