package org.blueventures.gemdroid.ui.roi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.theme.SkyBlue
import java.io.File

object RoiList {
    @Composable
    fun Screen(viewModel: RoiViewModel, filesDir: File, setAppBarState: (AppBarUpdate) -> Unit, snackbar: (String) -> Unit, roiClick: (File) -> Unit, floatingOnClick: () -> Unit) {
        setAppBarState(AppBarUpdate(title = "Regions of Interest"))

        val state by viewModel.state.collectAsState()
        val (toDelete, setDeleteRoi) = remember{ mutableStateOf<File?>(null) }

        if (toDelete != null) {
            DeleteDialog(viewModel, snackbar, toDelete) { setDeleteRoi(null) }
        }

        if (state.rois == null) {
            Progress()
            viewModel.refreshRois(filesDir)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = floatingOnClick, modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(Icons.Filled.Add, "Add new ROI")
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    ListView(viewModel, roiClick, setDeleteRoi)
                }
            }
        }
    }

    @Composable
    fun DeleteDialog(viewModel: RoiViewModel, snackbar: (String) -> Unit, toDelete: File, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Delete ROI") },
            text = { Text(text = "Really delete '${toDelete.name}'?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteRoi(toDelete) { success ->
                        onDismiss()
                        if (!success) {
                            snackbar("Failed to delete ${toDelete.name}")
                        }
                    }
                }) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                Button(onClick = {
                    onDismiss()
                }) {
                    Text(text = "Cancel")
                }
            },
        )
    }

    @Composable
    fun ListView(viewModel: RoiViewModel, roiClick: (File) -> Unit, setDeleteRoi: (File?) -> Unit) {
        val state by viewModel.state.collectAsState()
        state.rois?.let { rois ->
            if (rois.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Regions of Interest (ROIs) yet, create one by tapping the plus button!",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    items(rois) { dir ->
                        RoiRow(dir, roiClick, setDeleteRoi)
                        Divider(color = SkyBlue, thickness = 1.dp)
                    }
                }
            }
        }
    }

    @Composable
    fun RoiRow(dir: File, roiClick: (File) -> Unit, setDeleteRoi: (File?) -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { roiClick(dir) },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = dir.name, fontSize = 24.sp, modifier = Modifier.padding(24.dp))
            Icon(
                Icons.Filled.Delete, "Delete ROI", modifier = Modifier
                    .padding(20.dp)
                    .size(32.dp)
                    .clickable {
                        setDeleteRoi(dir)
                    }
            )
        }
    }
}