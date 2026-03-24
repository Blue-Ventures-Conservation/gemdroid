package org.blueventures.gemdroid.ui.analysis.cra.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.PolygonFile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.once
import java.io.InputStream

object Common {
    @Composable
    fun Screen(viewModel: CRAViewModel, temporal: String, snack: SnackFun, next: Click, back: Click, previous: String?, setLocal: (CRAFile) -> Unit, setRemote: (String) -> Unit) {
        val (remoteCRAs, setRemoteCRAs) = remember { mutableStateOf<Result<List<String>>?>(null) }

        when {
            remoteCRAs == null -> {
                Progress()
                Effect.Once {
                    viewModel.getRemoteCRAs(setRemoteCRAs)
                }
            }
            remoteCRAs.isFailure -> {
                Progress()
                val msg = remoteCRAs.exceptionOrNull()!!.localized(LocalContext.current)
                snack.once(msg)
                back.once()
            }
            else -> {
                val remotes = remoteCRAs.getOrNull()!!.toMutableList()
                if (previous != null && remotes.contains(previous)) {
                    remotes.remove(previous)
                }
                Selection(viewModel, temporal, remotes, snack, next, previous, setLocal, setRemote)
            }
        }
    }

    @Composable
    fun Selection(viewModel: CRAViewModel, temporal: String, remoteCRAs: List<String>, snack: SnackFun, next: Click, previous: String?, setLocal: (CRAFile) -> Unit, setRemote: (String) -> Unit) {
        Col.Col {
            val (craFile, setCRAFile) = remember { mutableStateOf<Result<CRAFile>?>(null) }
            val (remoteKey, setRemoteKey) = remember { mutableStateOf<String?>(null) }
            val (progress, setProgress) = remember { mutableStateOf(false) }
            val (checkedState, setCheckedState) = remember { mutableStateOf(false) }

            val askOverwrite = remember { mutableStateOf(false) }
            val overwrite = remember { mutableStateOf(false) }
            val shpName = remember { mutableStateOf("") }
            val craUris = remember { mutableStateOf<List<Uri>>(emptyList()) }
            val craCallback = remember { mutableStateOf<((Result<CRAFile>) -> Unit)>({}) }
            val overwriteState = OverwriteState(askOverwrite, overwrite, shpName, craUris, craCallback)

            when {
                progress -> Progress()
                remoteKey != null -> {
                    Effect.Once {
                        setRemote(remoteKey)
                        next()
                    }
                }
                craFile != null -> {
                    val ctx = LocalContext.current
                    Effect.Once {
                        when {
                            craFile.isSuccess -> {
                                setLocal(craFile.getOrNull()!!)
                                next()
                            }
                            else -> {
                                val exception = craFile.exceptionOrNull()!!
                                snack(exception.localized(ctx))
                                setCRAFile(null)
                            }
                        }
                    }
                }
                else -> {
                    val sp: (Boolean) -> Unit = { setProgress(it) }
                    val local: (Result<CRAFile>?) -> Unit = { setProgress(false); setCRAFile(it) }
                    if (remoteCRAs.isEmpty()) {
                        LocalCRA(viewModel, temporal, remoteCRAs, previous, local, sp, overwriteState)
                    } else {
                        LocalRemoteSwitch(viewModel, checkedState, setCheckedState, temporal, remoteCRAs, previous, local, setRemoteKey, sp, overwriteState)
                    }
                }
            }
        }
    }

    @Composable
    fun LocalRemoteSwitch(viewModel: CRAViewModel, checkedState: Boolean, setCheckedState: (Boolean) -> Unit, temporal: String, remoteCRAs: List<String>, previous: String?, setLocal: (Result<CRAFile>?) -> Unit, setRemote: (String) -> Unit, setProgress: (Boolean) -> Unit, overwriteState: OverwriteState) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.reuse_a_previously_uploaded_shapefile), fontSize = 16.sp)
            Switch(
                checked = checkedState,
                onCheckedChange = { setCheckedState(it) }
            )
        }

        if (checkedState) {
            RemoteCRA(temporal, remoteCRAs, setRemote)
        } else {
            LocalCRA(viewModel, temporal, remoteCRAs, previous, setLocal, setProgress, overwriteState)
        }
    }

    class BadName(val shpName: String): Throwable("")

    data class OverwriteState(
        val askOverwrite: MutableState<Boolean>,
        val overwrite: MutableState<Boolean>,
        val shpName: MutableState<String>,
        val craUris: MutableState<List<Uri>>,
        val craCallback: MutableState<(Result<CRAFile>) -> Unit>
    )

    @Composable
    fun LocalCRA(viewModel: CRAViewModel, temporal: String, remoteCRAs: List<String>, previous: String?, setLocal: (Result<CRAFile>?) -> Unit, setProgress: (Boolean) -> Unit, state: OverwriteState) {
        val context = LocalContext.current
        when {
            state.askOverwrite.value -> {
                Progress()
                OverwriteDialog(state.shpName.value, onDismiss = {
                    state.overwrite.value = false
                    state.askOverwrite.value = false
                    setLocal(Result.failure(NoStack(R.string.please_reuse_poly_file)))
                }) {
                    state.overwrite.value = true
                    state.askOverwrite.value = false
                }
            }
            state.overwrite.value -> {
                makeStreams(context, viewModel::background, state.craUris.value) { streams ->
                    viewModel.validateLocalCRA(streams.streams, streams.names, remoteCRAs, previous, true) { craResult ->
                        if (craResult.isSuccess) {
                            val crafile = craResult.getOrNull()!!
                            crafile.overwrite = true
                            state.craCallback.value.invoke(Result.success(crafile))
                        } else {
                            state.craCallback.value.invoke(craResult)
                        }
                        state.overwrite.value = false
                    }
                }
                setProgress(true)
            }
            else -> {
                PolygonFile.Result(stringResource(R.string.select_a_temporal_shapefile).format(temporal), { uris, callback ->
                    makeStreams(context, viewModel::background, uris) { streams ->
                        viewModel.validateLocalCRA(streams.streams, streams.names, remoteCRAs, previous, false) { result ->
                            if (result.isFailure) {
                                val exception = result.exceptionOrNull()!!
                                if (exception as? BadName != null) {
                                    state.shpName.value = exception.shpName
                                    state.craUris.value = uris
                                    state.craCallback.value = callback
                                    state.askOverwrite.value = true
                                }
                            }

                            callback(result)
                        }
                    }
                }, { result: Result<CRAFile> ->
                    if (result.isSuccess || result.exceptionOrNull()!! as? BadName == null) {
                        setLocal(result)
                    } else {
                        setProgress(false)
                    }
                }, {
                    setProgress(true)
                })
            }
        }
    }

    fun makeStreams(context: Context, background: (() -> PolygonFile.Streams, (PolygonFile.Streams) -> Unit) -> Unit, uris: List<Uri>, callback: (PolygonFile.Streams) -> Unit) {
        background({
            val strms = mutableListOf<InputStream?>()
            val names = mutableListOf<String?>()
            uris.forEach { uri ->
                strms.add(context.contentResolver.openInputStream(uri))
                names.add(contentDisplayName(context, uri))
            }
            PolygonFile.Streams(strms, names)
        }) { streams ->
            callback(streams)
        }
    }

    private fun contentDisplayName(context: Context, uri: Uri): String? {
        var name: String? = null

        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
                cursor.close()
            }
        }

        return name
    }

    @Composable
    fun OverwriteDialog(shpName: String, onDismiss: Click, onOverwrite: Click) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.overwrite_saved_cra)) },
            text = { Text(text = stringResource(R.string.overwrite_the_existing_cra_fmt).format(shpName)) },
            confirmButton = {
                Butt.Text(stringResource(R.string.overwrite), click = onOverwrite)
            },
            dismissButton = {
                Butt.Text(stringResource(R.string.cancel), click = onDismiss)
            }
        )
    }

    @Composable
    fun RemoteCRA(temporal: String, remoteCRAs: List<String>, setRemote: (String) -> Unit) {
        var nextEnabled by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf("") }
        Dropdown(title = stringResource(R.string.select_a_temporal_shapefile).format(temporal), labels = remoteCRAs) { i ->
            selected= remoteCRAs[i]
            nextEnabled= true
        }

        Butt.Next(nextEnabled) {
            setRemote(selected)
        }
    }
}