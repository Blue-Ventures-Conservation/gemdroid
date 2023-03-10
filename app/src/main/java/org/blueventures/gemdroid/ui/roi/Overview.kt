package org.blueventures.gemdroid.ui.roi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.File

object Overview {
    @Composable
    fun Screen(viewModel: RoiViewModel, filesDir: File, snack: SnackFun, done: Click) {
        val (saving, setSaving) = remember { mutableStateOf(false) }

        if (saving) {
            Progress()
        } else {
            OverviewDetails(viewModel, filesDir, snack, done, setSaving)
        }
    }

    @Composable
    fun OverviewDetails(viewModel: RoiViewModel, filesDir: File, snack: SnackFun, done: Click, saving: (Boolean) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Overview", fontSize = 32.sp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "Name: ", fontSize = 18.sp)
                    Text(text = "Contemporary Years: ", fontSize = 18.sp)
                    Text(text = "Contemporary Months: ", fontSize = 18.sp)
                    Text(text = "Historical Years: ", fontSize = 18.sp)
                    Text(text = "Historical Months: ", fontSize = 18.sp)
                    Text(text = "Polygon ROI: ", fontSize = 18.sp)
                    Text(text = "Spectral Indices: ", fontSize = 18.sp)
                }
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = viewModel.name, fontSize = 18.sp)
                    Text(text = "${viewModel.contemporaryYearStart} - ${viewModel.contemporaryYearEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.contemporaryMonthStart} - ${viewModel.contemporaryMonthEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.historicalYearStart} - ${viewModel.historicalYearEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.historicalMonthStart} - ${viewModel.historicalMonthEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.points.size} points, ${"%,d".format(viewModel.polygonArea().toInt())} km²", fontSize = 18.sp)
                    Text(text = "${viewModel.indices.list()}", fontSize = 18.sp)
                }
            }
            Button(onClick = {
                saving(true)
                viewModel.saveRoi(filesDir) { result ->
                    saving(false)
                    if (result.isSuccess) {
                        done()
                    } else {
                        snack(result.exceptionOrNull()!!.message!!)
                    }
                }
            }) {
                Text(text = "Done", fontSize = 20.sp)
            }
        }
    }
}