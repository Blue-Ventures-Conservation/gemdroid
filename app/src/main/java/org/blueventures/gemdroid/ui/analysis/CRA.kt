package org.blueventures.gemdroid.ui.analysis

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.analysis.AnalysisState
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

typealias SelectCRA = @Composable (AnalysisViewModel, State<AnalysisState>, List<String>, SnackFun) -> Unit

object CRA {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, snack: SnackFun, back: Click, selectCRA: SelectCRA) {
        val state = viewModel.state.collectAsState()

        when {
            state.value.remoteCRAs == null -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    viewModel.getRemoteCRAs()
                }
            }
            state.value.remoteCRAs!!.isFailure -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    snack(state.value.remoteCRAs!!.exceptionOrNull()!!.message!!)
                    back()
                }
            }
            else -> {
                selectCRA(viewModel, state, state.value.remoteCRAs!!.getOrNull()!!, snack)
            }
        }

        BackHandler {
            back()
        }
    }

    @Composable
    fun Selection(viewModel: AnalysisViewModel, temporal: String, remoteCRAs: List<String>, result: MutableState<List<Uri>?>, setValidating: (Boolean) -> Unit, next: Click, fromStorage: (String) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (remoteCRAs.isEmpty()) {
                LocalCRA(viewModel, temporal, result, setValidating, next)
            } else {
                LocalRemoteSwitch(viewModel, temporal, remoteCRAs, result, setValidating, next, fromStorage)
            }
        }
    }

    @Composable
    fun LocalRemoteSwitch(viewModel: AnalysisViewModel, temporal: String, remoteCRAs: List<String>, result: MutableState<List<Uri>?>, setValidating: (Boolean) -> Unit, next: Click, fromStorage: (String) -> Unit) {
        val checkedState = remember { mutableStateOf(true) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checkedState.value) {
                Text(text = "Re-use a previously uploaded shapefile", fontSize = 16.sp)
            } else {
                Text(text = "Select a shapefile from local files", fontSize = 16.sp)
            }
            Switch(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it }
            )
        }

        if (checkedState.value) {
            RemoteCRA(remoteCRAs, next, fromStorage)
        } else {
            LocalCRA(viewModel, temporal, result, setValidating, next)
        }
    }

    @Composable
    fun LocalCRA(viewModel: AnalysisViewModel, temporal: String, result: MutableState<List<Uri>?>, setValidating: (Boolean) -> Unit, next: Click) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            setValidating(true)
            result.value = it
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Select a $temporal CRA shapefile.",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(modifier = Modifier.fillMaxWidth(), text = "You should select all of: ", fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(modifier = Modifier.fillMaxWidth(), text = ".shp, .dbf, .shx, .prj", fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Or you can select a .zip that contains these.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            result.value?.forEach { uri ->
                contentDisplayName(LocalContext.current, uri)?.let { name ->
                    Text(text = name, fontSize = 16.sp)
                }
            }
        }
        if (result.value?.isEmpty() != false) {
            Button(onClick = {
                launcher.launch(arrayOf("*/*"))
            }) {
                Text(text = "Select Shapefile", fontSize = 20.sp)
            }
        } else {
            Button(onClick = {
                viewModel.clearStage()
                next()
            }) {
                Text(text = "Next", fontSize = 20.sp)
            }
        }
    }

    @Composable
    fun RemoteCRA(remoteCRAs: List<String>, next: Click, fromStorage: (String) -> Unit) {
        val nextEnabled = remember { mutableStateOf(false) }
        val selected = remember { mutableStateOf("") }
        Dropdown(title = "Select Shapefile:", labels = remoteCRAs) { i ->
            selected.value = remoteCRAs[i]
            nextEnabled.value = true
        }

        Button(
            enabled = nextEnabled.value,
            onClick = {
                fromStorage(selected.value)
                next()
            }
        ) {
            Text(text = "Next", fontSize = 16.sp)
        }
    }

    fun contentDisplayName(context: Context, uri: Uri): String? {
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
}