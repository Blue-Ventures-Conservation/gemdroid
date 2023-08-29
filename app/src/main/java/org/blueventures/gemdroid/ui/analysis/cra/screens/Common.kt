package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
                Effect.Once {
                    snack(msg)
                    back()
                }
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
            if (remoteCRAs.isEmpty()) {
                LocalCRA(viewModel, temporal, snack, remoteCRAs, previous, next, setLocal)
            } else {
                LocalRemoteSwitch(viewModel, temporal, snack, remoteCRAs, previous, next, setLocal, setRemote)
            }
        }
    }

    @Composable
    fun LocalRemoteSwitch(viewModel: CRAViewModel, temporal: String, snack: SnackFun, remoteCRAs: List<String>, previous: String?, next: Click, setLocal: (CRAFile) -> Unit, setRemote: (String) -> Unit) {
        val checkedState = remember { mutableStateOf(true) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checkedState.value) {
                Text(text = stringResource(R.string.reuse_a_previously_uploaded_shapefile), fontSize = 16.sp)
            } else {
                Text(text = stringResource(R.string.select_a_shapefile_from_local_files), fontSize = 16.sp)
            }
            Switch(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it }
            )
        }

        if (checkedState.value) {
            RemoteCRA(temporal, remoteCRAs, next, setRemote)
        } else {
            LocalCRA(viewModel, temporal, snack, remoteCRAs, previous, next, setLocal)
        }
    }

    @Composable
    fun LocalCRA(viewModel: CRAViewModel, temporal: String, snack: SnackFun, remoteCRAs: List<String>, previous: String?, next: Click, setLocal: (CRAFile) -> Unit) {
        Shapefile.Screen(stringResource(R.string.select_a_temporal_shapefile).format(temporal), { bg ->
            viewModel.background(bg)
        }, { streams, callback ->
            viewModel.validateLocalCRA(streams.streams, streams.names, remoteCRAs, previous, callback)
        }, { err ->
            snack(err)
        }, { cra ->
            setLocal(cra)
            next()
        })
    }

    @Composable
    fun RemoteCRA(temporal: String, remoteCRAs: List<String>, next: Click, setRemote: (String) -> Unit) {
        val nextEnabled = remember { mutableStateOf(false) }
        val selected = remember { mutableStateOf("") }
        Dropdown(title = stringResource(R.string.select_a_temporal_shapefile).format(temporal), labels = remoteCRAs) { i ->
            selected.value = remoteCRAs[i]
            nextEnabled.value = true
        }

        Butt.Next(nextEnabled.value) {
            setRemote(selected.value)
            next()
        }
    }
}