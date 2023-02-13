package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.Progress

object Buffer {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, snackbar: (String) -> Unit, backClick: () -> Unit) {
        val state by viewModel.state.collectAsState()

        when {
            state.roi == null -> {
                Progress()
                viewModel.getROI()
            }
            state.roi!!.isFailure -> {
                snackbar(state.roi!!.toString())
                backClick()
            }
            state.buffers == null -> {
                Progress()
                viewModel.getBuffersFile()
            }
            !Buffers.isEmpty(state.buffers) -> {
                Buffers(state.buffers!!)
            }
            state.buffersResult == null -> {
                Progress()
                state.roi!!.getOrNull()?.let {
                    viewModel.getBuffers(it)
                }
            }
            state.buffersResult is ApiResult.Error -> {
                RefreshableError(viewModel, state.roi!!.getOrNull()!!)
            }
            state.buffersResult is ApiResult.Success -> {
                val buffers = state.buffersResult!!.data!!
                viewModel.saveBuffersFile(buffers)
                Buffers(buffers)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun RefreshableError(viewModel: AnalysisViewModel, roi: ROI.Data) {
        var refreshing by remember { mutableStateOf(false) }

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = refreshing),
            onRefresh = {
                refreshing = true
                viewModel.getBuffers(roi) {
                    refreshing = false
                }
            },
            indicator = { st, trigger ->
                SwipeRefreshIndicator(state = st, refreshTriggerDistance = trigger)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "ROI Buffer", fontSize = 32.sp)
                Text(
                    text = "Could not reach our server! You can swipe down to try again.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
                Spacer(modifier = Modifier.size(0.dp))
            }
        }
    }

    @Composable
    fun Buffers(buffers: Buffers.Data) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "ROI Buffer", fontSize = 32.sp)
            Chart(buffers)
            // TODO buffer choice
        }
    }

    @Composable
    fun Chart(buffers: Buffers.Data) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                AnyChartView(context)
            },
            update = { chart ->
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
        )
    }
}