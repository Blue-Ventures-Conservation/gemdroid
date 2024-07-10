package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.hectareInMeters
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
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object Buffer {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun, back: Click) {
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

    @Composable
    fun BufferChoice(viewModel: AnalysisViewModel, buffers: Buffers, snack: SnackFun, back: Click, saving: (Boolean) -> Unit) {
        val (bufferDist, setBufferDist) = remember { mutableStateOf(Pair(-1, false)) }

        Col.Col {
            val ctx = LocalContext.current
            Info.Txt(text = stringResource(R.string.tap_bars_hectares))
            Chart(buffers)
            Dropdown(title = stringResource(R.string.select_buffer_distance), labels = buffers.buffers.keys) { i ->
                setBufferDist(Pair(buffers.buffers.vals[i], true))
            }
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
        val title = stringResource(R.string.mangrove_area_by_buffer)
        val xLabel = stringResource(R.string.shoreline_buffer_km)
        val yLabel = stringResource(R.string.area_ha)
        val sums = buffers.sums
        val bufferKeys = buffers.buffers.keys
        val data = mutableListOf<DataEntry>()
        sums.keys.forEachIndexed { i, sumKey ->
            val key = if (bufferKeys.size > i) {
                bufferKeys[i].split(" ").first()
            } else {
                sumKey
            }
            data.add(ValueDataEntry(key, (sums.vals[i].toDouble()/hectareInMeters).toInt()))
        }
        Charts.BarChart(title, xLabel, yLabel, data, "km", "ha", true)
    }
}