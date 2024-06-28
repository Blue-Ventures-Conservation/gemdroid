package org.blueventures.gemdroid.ui.roi.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.Roi
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.once
import org.blueventures.gemdroid.ui.theme.SkyBlue
import java.io.File

object RoiList {
    interface DirHolder {
        var roiDir: File
    }

    @Composable
    fun Screen(viewModel: RoiViewModel, dirHolder: DirHolder, filesDir: File, appBar: AppBar, snack: SnackFun, next: Click, floating: Click) {
        val (wentNext, setWentNext) = remember { mutableStateOf(false) }
        if (!wentNext) {
            Effect.Once { viewModel.clearState() }
            Layout(viewModel, dirHolder, filesDir, appBar, snack, {
                setWentNext(true)
                next()
            }) {
                setWentNext(true)
                floating()
            }
        }
    }

    @Composable
    fun Layout(viewModel: RoiViewModel, dirHolder: DirHolder, filesDir: File, appBar: AppBar, snack: SnackFun, next: Click, floating: Click) {
        appBar.Update(AppBarUpdate(title = stringResource(R.string.regions_of_interest)))

        val (rois, setRois) = remember { mutableStateOf<Result<List<File>>?>(null) }

        when {
            rois == null -> {
                Progress()
                viewModel.refreshRois(filesDir, setRois)
            }
            rois.isFailure -> {
                snack.once(rois.exceptionOrNull()!!.localized(LocalContext.current))
            }
            else -> {
                val list = rois.getOrNull()!!
                viewModel.rois = list

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ListView(viewModel, snack, list, setRois, dirHolder, next, floating)
                    }
                    FloatingActionButton(
                        onClick = floating,
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Filled.Add, stringResource(R.string.add_new_region))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListView(viewModel: RoiViewModel, snack: SnackFun, rois: List<File>, setRois: (Result<List<File>>?) -> Unit, dirHolder: DirHolder, next: Click, floating: Click) {
        if (rois.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_rois_yet),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            val sheetState = rememberModalBottomSheetState()
            val (showSheet, setShowSheet) = remember { mutableStateOf<File?>(null) }
            val (toDelete, setDeleteRoi) = remember { mutableStateOf<File?>(null) }
            val (toDupe, setDupeRoi) = remember { mutableStateOf<File?>(null) }

            toDupe?.let {
                Roi.Loader({ callback ->
                    viewModel.getROI(it, callback)
                }, snack, {}) {
                    viewModel.importROI(stringResource(R.string.copy_of), it)
                    floating()
                }
            } ?: run {
                LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    items(rois) { dir ->
                        RoiRow(dir, dirHolder, next, setShowSheet)
                        HorizontalDivider(color = SkyBlue, thickness = 1.dp)
                    }
                }
            }

            showSheet?.let {
                BottomSheet(sheetState, showSheet, setShowSheet, setDupeRoi, setDeleteRoi)
            }
            toDelete?.let {
                DeleteDialog(viewModel, snack, toDelete, { setDeleteRoi(null) }) { setDeleteRoi(null); setRois(null) }
            }
        }
    }

    @Composable
    fun RoiRow(dir: File, dirHolder: DirHolder, next: Click, setShowSheet: (File?) -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    dirHolder.roiDir = dir
                    next()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // weight here asks compose to measure the icon first, before doing layout of text, so that text overflow doesn't obscure the icon
            Text(text = dir.name, fontSize = 24.sp, modifier = Modifier.padding(24.dp).weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(
                Icons.Filled.MoreVert, stringResource(R.string.roi_options_menu), modifier = Modifier
                    .padding(20.dp)
                    .size(32.dp)
                    .clickable { setShowSheet(dir) }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomSheet(state: SheetState, dir: File, setShowSheet: (File?) -> Unit, setDupeRoi: (File?) -> Unit, setDeleteRoi: (File?) -> Unit) {
        ModalBottomSheet(onDismissRequest = {
            setShowSheet(null)
        }, sheetState = state) {
            Text(dir.name, modifier = Modifier.fillMaxWidth(), fontSize = 16.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            SheetRow({
                setDupeRoi(dir)
            }, Icons.Filled.ContentCopy, R.string.copy_region, R.string.copy_roi)
            SheetRow({
                setDeleteRoi(dir)
            }, Icons.Filled.Delete, R.string.delete_region, R.string.delete_roi)
            Spacer(modifier = Modifier.padding(64.dp))
        }
    }

    @Composable
    fun SheetRow(click: Click, icon: ImageVector, @StringRes descrip: Int, @StringRes label: Int) {
        Row(modifier = Modifier
            .padding(start = 4.dp, top = 4.dp)
            .clickable(onClick = click),
            verticalAlignment = Alignment.CenterVertically)
        {
            Icon(
                icon, stringResource(descrip), modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
            )
            Info.Txt(stringResource(label))
        }
    }

    @Composable
    fun DeleteDialog(viewModel: RoiViewModel, snack: SnackFun, toDelete: File, onDismiss: Click, onDelete: Click) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.delete_roi)) },
            text = { Text(text = stringResource(R.string.really_delete).format(toDelete.name)) },
            confirmButton = {
                val ctx = LocalContext.current
                Butt.Text(stringResource(R.string.delete)) {
                    viewModel.deleteRoi(toDelete) { result ->
                        onDelete()
                        if (result.isFailure) {
                            snack(result.exceptionOrNull()!!.localized(ctx))
                        }
                    }
                }
            },
            dismissButton = {
                Butt.Text(stringResource(R.string.cancel), click = onDismiss)
            }
        )
    }
}