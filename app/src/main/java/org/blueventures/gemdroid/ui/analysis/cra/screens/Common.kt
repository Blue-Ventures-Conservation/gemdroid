package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
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
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.once

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
                                snack(craFile.exceptionOrNull()!!.localized(ctx))
                                setCRAFile(null)
                            }
                        }
                    }
                }
                else -> {
                    val sp = { setProgress(true) }
                    val local: (Result<CRAFile>?) -> Unit = { setProgress(false); setCRAFile(it) }
                    if (remoteCRAs.isEmpty()) {
                        LocalCRA(viewModel, temporal, remoteCRAs, previous, local, sp)
                    } else {
                        LocalRemoteSwitch(viewModel, checkedState, setCheckedState, temporal, remoteCRAs, previous, local, setRemoteKey, sp)
                    }
                }
            }
        }
    }

    @Composable
    fun LocalRemoteSwitch(viewModel: CRAViewModel, checkedState: Boolean, setCheckedState: (Boolean) -> Unit, temporal: String, remoteCRAs: List<String>, previous: String?, setLocal: (Result<CRAFile>?) -> Unit, setRemote: (String) -> Unit, setProgress: () -> Unit) {
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
            LocalCRA(viewModel, temporal, remoteCRAs, previous, setLocal, setProgress)
        }
    }

    @Composable
    fun LocalCRA(viewModel: CRAViewModel, temporal: String, remoteCRAs: List<String>, previous: String?, setLocal: (Result<CRAFile>?) -> Unit, setProgress: () -> Unit) {
        Shapefile.Result(stringResource(R.string.select_a_temporal_shapefile).format(temporal), viewModel::background, { streams, callback ->
            viewModel.validateLocalCRA(streams.streams, streams.names, remoteCRAs, previous, callback)
        }, setLocal, setProgress)
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