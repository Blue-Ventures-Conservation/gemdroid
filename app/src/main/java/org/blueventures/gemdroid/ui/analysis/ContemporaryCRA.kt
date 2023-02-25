package org.blueventures.gemdroid.ui.analysis

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Progress

object ContemporaryCRA {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, setAppBarState: (AppBarUpdate) -> Unit, snackbar: (String) -> Unit, nextClick: () -> Unit, backClick: () -> Unit) {
        setAppBarState(AppBarUpdate(title = "Classification Reference Areas (CRAs)"))
        ContemporaryCRA(viewModel, snackbar, nextClick, backClick)
    }

    @Composable
    fun ContemporaryCRA(viewModel: AnalysisViewModel, snackbar: (String) -> Unit, nextClick: () -> Unit, backClick: () -> Unit) {
        val state by viewModel.state.collectAsState()

        when {
            state.remoteCRAs == null -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    viewModel.getRemoteCRAs()
                }
            }
            state.remoteCRAs!!.isFailure -> {
                Progress()
                LaunchedEffect(key1 = true) {
                    snackbar(state.remoteCRAs!!.exceptionOrNull()!!.message!!)
                    backClick()
                }
            }
            else -> {
                SelectCRA(state.remoteCRAs!!.getOrNull()!!, viewModel, snackbar, nextClick)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun SelectCRA(remoteCRAs: List<String>, viewModel: AnalysisViewModel, snackbar: (String) -> Unit, nextClick: () -> Unit) {
        val result = remember { mutableStateOf<List<Uri>?>(null) }
        val (validating, setValidating) = remember { mutableStateOf(false) }

        if (validating) {
            Progress()
            // validate CRAs
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 64.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (remoteCRAs.isEmpty()) {
                    LocalCRA(viewModel, result, setValidating, nextClick)
                } else {
                    LocalRemoteSwitch(remoteCRAs, viewModel, result, setValidating, nextClick)
                }
            }
        }
    }

    @Composable
    fun LocalRemoteSwitch(remoteCRAs: List<String>, viewModel: AnalysisViewModel, result: MutableState<List<Uri>?>, setValidating: (Boolean) -> Unit, nextClick: () -> Unit) {
        val checkedState = remember { mutableStateOf(true) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checkedState.value) {
                Text(text = "Re-use a previously uploaded CRA", fontSize = 16.sp)
            } else {
                Text(text = "Select a CRA from local files", fontSize = 16.sp)
            }
            Switch(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it }
            )
        }

        if (checkedState.value) {
            RemoteCRA(remoteCRAs, nextClick)
        } else {
            LocalCRA(viewModel, result, setValidating, nextClick)
        }
    }

    @Composable
    fun LocalCRA(viewModel: AnalysisViewModel, result: MutableState<List<Uri>?>, setValidating: (Boolean) -> Unit, nextClick: () -> Unit) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            setValidating(true)
            result.value = it
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Select a Contemporary CRA shapefile.",
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
            result.value?.forEach {
                LocalContext.current.contentResolver.query(it, null, null, null, null)?.let { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val name = cursor.getString(nameIndex)
                    cursor.close()
                    Text(text = name, fontSize = 16.sp)
                }
            }
        }
        if (result.value?.isEmpty() != false) {
            Button(onClick = {
                launcher.launch(arrayOf("*/*"))
            }) {
                Text(text = "Select CRA", fontSize = 20.sp)
            }
        } else {
            Button(onClick = {
                viewModel.clearStage()
                nextClick()
            }) {
                Text(text = "Next", fontSize = 20.sp)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RemoteCRA(remoteCRAs: List<String>, nextClick: () -> Unit) {
        val (expanded, setExpanded) = remember { mutableStateOf(false) }
        val (selected, setSelected) = remember { mutableStateOf("") }
        val nextEnabled = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select CRA",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { setExpanded(!expanded) }
            ) {
                TextField(
                    selected,
                    {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { setExpanded(false) }
                ) {
                    remoteCRAs.forEach { cra ->
                        DropdownMenuItem(
                            onClick = {
                                setSelected(cra)
                                nextEnabled.value = true
                                setExpanded(false)
                            },
                            text = {
                                Text(text = cra, fontSize = 16.sp)
                            },
                        )
                    }
                }
            }
        }

        Button(
            enabled = nextEnabled.value,
            onClick = nextClick,
        ) {
            Text(text = "Next", fontSize = 16.sp)
        }
    }
}