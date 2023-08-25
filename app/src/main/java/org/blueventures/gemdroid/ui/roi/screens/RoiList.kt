package org.blueventures.gemdroid.ui.roi.screens

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
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.theme.SkyBlue
import java.io.File

object RoiList {
    @Composable
    fun Screen(viewModel: RoiViewModel, filesDir: File, appbar: AppBarFun, snack: SnackFun, roiClick: (File) -> Unit, floatingOnClick: () -> Unit) {
        appbar(AppBarUpdate(title = "Regions of Interest"))

        val (rois, setRois) = remember { mutableStateOf<Result<List<File>>?>(null) }
        val (toDelete, setDeleteRoi) = remember { mutableStateOf<File?>(null) }

        if (toDelete != null) {
            DeleteDialog(viewModel, snack, toDelete) { setDeleteRoi(null); setRois(null) }
        }

        when {
            rois == null -> {
                Progress()
                viewModel.refreshRois(filesDir, setRois)
            }
            rois.isFailure -> {
                snack(rois.exceptionOrNull()!!.localized(LocalContext.current))
            }
            else -> {
                val list = rois.getOrNull()!!
                viewModel.rois = list

                Box(modifier = Modifier.fillMaxSize()) {
                    FloatingActionButton(
                        onClick = floatingOnClick,
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Filled.Add, "Add new ROI")
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        ListView(list, roiClick, setDeleteRoi)
                    }
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
                val ctx = LocalContext.current
                Butt.Text("DELETE") {
                    viewModel.deleteRoi(toDelete) { result ->
                        onDismiss()
                        if (result.isFailure) {
                            snackbar(result.exceptionOrNull()!!.localized(ctx))
                        }
                    }
                }
            },
            dismissButton = {
                Butt.Text("Cancel", onClick = onDismiss)
            }
        )
    }

    @Composable
    fun ListView(rois: List<File>, roiClick: (File) -> Unit, setDeleteRoi: (File?) -> Unit) {
        if (rois.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Regions of Interest (ROIs) yet, create one by tapping the plus button!",
                    fontSize = 16.sp,
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
                    .clickable { setDeleteRoi(dir) }
            )
        }
    }
}