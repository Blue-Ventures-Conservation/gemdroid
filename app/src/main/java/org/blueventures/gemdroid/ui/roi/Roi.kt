package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.model.roi.RoiViewModel
import java.io.File

object Roi {
    private var refreshJob: Job? = null

    @Composable
    fun List(filesDir : File, viewModel: RoiViewModel = viewModel(), roiClick: (File) -> Unit, floatingOnClick: () -> Unit) {
        val state by viewModel.state.collectAsState()

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(onClick = floatingOnClick, modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd)) {
                Icon(Icons.Filled.Add, "")
            }

            if (state.rois == null) {
                Progress()

                val active = refreshJob?.isActive ?: false
                if (!active) {
                    refreshJob = viewModel.refreshRois(filesDir)
                }
            } else {
                state.rois?.let { rois ->
                    if (rois.isEmpty()) {
                        Text(
                            text = "No Regions of Interest (ROIs) yet, create one by tapping the plus button!",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {

                    }
                }
            }
        }
    }

    @Composable
    fun Item() {

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Name(viewModel: RoiViewModel = viewModel(), backClick: () -> Unit, nextClick: (String) -> Unit) {
        val state by viewModel.state.collectAsState()
        var text by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Please enter a name") }
            )
            Button(
                modifier = Modifier.padding(24.dp),
                onClick = {

            }) {
                Text("Next", fontSize = 24.sp)
            }
            BackHandler {
                backClick()
            }
        }
    }

    @Composable
    fun Dates() {

    }

    @Composable
            /**
             * Create an ROI
             */
    fun Polygon() {

    }

    @Composable
    fun Progress() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(152.dp))
        }
    }
}

object RoiRoutes {
    const val list = "roi"
    const val name = "roi_name"
    const val dates = "roi_dates"
    const val polygon = "roi_polygon"
}

object RoiView {
    const val roiArg = "roi"
    const val routeWithArgs = "roi/{$roiArg}"
}