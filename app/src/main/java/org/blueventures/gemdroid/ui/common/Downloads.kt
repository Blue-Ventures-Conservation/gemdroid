package org.blueventures.gemdroid.ui.common

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Downloads {
    interface VisualizeHolder {
        var visualize: Boolean
    }

    interface NamedExport {
        val title: String
        val filename: String
        fun getDownloadUri(callback: (Result<Uri>) -> Unit)
    }

    interface ExportList {
        fun saveResults(results: TasksResults): Job
        fun loadResults(callback: (Result<TasksResults>) -> Unit): Job
        fun getResults(callback: (ApiResult<TasksResults>) -> Unit)
        fun list(): List<NamedExport>
    }

    @Composable
    fun <T> Screen(
        holder: VisualizeHolder,
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: (Boolean, (ApiResult<T>) -> Unit) -> Unit,
        save: (T) -> Unit,
        clear: Click,
        convert: @Composable (T) -> ExportList,
    ) {
        val (shouldChoose, setShouldChoose) = remember { mutableStateOf(false) }
        val (chosen, setChosen) = remember { mutableStateOf(false) }

        when {
            shouldChoose -> {
                ChooseType(holder) {
                    setChosen(true)
                    setShouldChoose(false)
                }
            }
            else -> {
                GetRemote.Save(getLocal = { callback ->
                    getLocal { result ->
                        when {
                            result.isSuccess -> callback(result)
                            else -> {
                                if (chosen) {
                                    callback(result)
                                } else {
                                    setShouldChoose(true)
                                }
                            }
                        }
                    }
                }, getRemote = { callback ->
                    getRemote(holder.visualize, callback)
                }, save = save, checkExpires = true) { exports ->
                    List(convert(exports), clear)
                }
            }
        }
    }

    @Composable
    fun ChooseType(holder: VisualizeHolder, setChosen: () -> Unit) {
        Col.Col {
            val (allBands, setAllBands) = remember { mutableStateOf(false) }

            if (allBands) {
                AllBandsDialog({
                    holder.visualize = false
                    setChosen()
                }) {
                    setAllBands(false)
                }
            }

            val (checked, setChecked) = remember { mutableStateOf(false) }

            Info.Row {
                Info.Txt(text = stringResource(R.string.include_all_bands))
                Checkbox(checked = checked, onCheckedChange = setChecked)
            }
            Butt.Text(stringResource(R.string.prepare_files)) {
                if (checked) {
                    setAllBands(true)
                } else {
                    holder.visualize = true
                    setChosen()
                }
            }
        }
    }

    @Composable
    fun AllBandsDialog(confirm: Click, onDismiss: Click) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Info.Txt(stringResource(R.string.download_all_bands)) },
            text = { Info.Txt(stringResource(R.string.large_file_warning)) },
            confirmButton = {
                Butt.Text(stringResource(android.R.string.ok), click = confirm)
            },
            dismissButton = {
                Butt.Text(stringResource(R.string.cancel), click = onDismiss)
            }
        )
    }

    @Composable
    fun List(exports: ExportList, clear: Click) {
        GetRemote.Save(getLocal = exports::loadResults, getRemote = exports::getResults, save = exports::saveResults) { res ->
            val (results, setResults) = remember { mutableStateOf(res) }
            Refresh({ stop ->
                if (results.completed()) {
                    stop()
                } else {
                    exports.getResults { result ->
                        stop()
                        if (result is ApiResult.Success) {
                            val data = result.data!!
                            exports.saveResults(data)
                            setResults(data)
                        }
                    }
                }
            }) {
                Col.MidPad(scroll = true) {
                    var failure = false
                    Info.Block {
                        Info.Row(verticalPadding = 16.dp) {
                            Icon(Icons.Filled.ArrowDownward, stringResource(R.string.pull_down_to_refresh))
                            Info.Txt(stringResource(R.string.pull_down_to_refresh))
                            Icon(Icons.Filled.ArrowDownward, stringResource(R.string.pull_down_to_refresh))
                        }
                        Info.BlueLine()
                        exports.list().forEachIndexed { i, export ->
                            val result = results.results[i]
                            val failed = result.error != null
                            if (failed && !failure) failure = true
                            DownloadRow(export, result.success, failed)
                            Info.BlueLine()
                        }
                    }

                    if (failure) {
                        Col.DashboardButton(stringResource(R.string.clear), clear)
                    }
                }
            }
        }
    }

    @Composable
    fun DownloadRow(export: NamedExport, succeeded: Boolean, failed: Boolean) {
        val (downloadStarted, setDownloadStarted) = remember { mutableStateOf(false) }

        val ctx = LocalContext.current
        Info.Row(verticalPadding = 16.dp, enabled = succeeded && !downloadStarted, click = {
            setDownloadStarted(true)
            export.getDownloadUri { result ->
                when {
                    result.isSuccess -> {
                        downloadFile(ctx, result.getOrNull()!!, export.title, export.filename)
                    }
                }
            }
        }) {
            Info.Txt("${export.title}:")
            val iconMod = Modifier.size(36.dp)
            when {
                downloadStarted -> Icon(Icons.Rounded.Downloading, stringResource(R.string.download_started), iconMod.background(LightGreen))
                failed -> Icon(Icons.Rounded.Close, stringResource(R.string.download_has_failed), iconMod.background(MildRed))
                succeeded -> Icon(Icons.Rounded.Download, stringResource(R.string.download_this_file), iconMod.background(SkyBlue))
                else -> {
                    Box(modifier = iconMod) {
                        Progress()
                    }
                }
            }
        }
    }

    private fun downloadFile(context: Context, uri: Uri, title: String, filename: String) {
        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs()
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
            val request = DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            manager.enqueue(request)
        } catch (e: Exception) {
            // pass
        }
    }
}