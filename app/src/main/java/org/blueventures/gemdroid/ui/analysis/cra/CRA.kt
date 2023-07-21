package org.blueventures.gemdroid.ui.analysis.cra

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.cra.CRAFile
import org.blueventures.gemdroid.model.analysis.cra.CraViewModel
import org.blueventures.gemdroid.model.analysis.cra.HistoricalChoice
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.InputStream

object CRA {
    object Routes {
        const val cont_cra = "analysis_cra_cont"
        const val hist_choice = "analysis_cra_hist_choice"
        const val hist_cra = "analysis_cra_hist"
        const val cra_fields = "analysis_cra_fields"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: CraViewModel, appBar: AppBarFun, snack: SnackFun) {
        b.composable(Routes.cont_cra) {
            ContemporaryCRA.Screen(viewModel, appBar, snack, {
                nav.navigate(Routes.hist_choice)
            }) {
                nav.popClear(Analysis.Routes.dashboard)
            }
        }
        b.composable(Routes.hist_choice) {
            ChooseHistorical.Screen(viewModel, {
                when (viewModel.historicalChoice) {
                    HistoricalChoice.SEPARATE -> {
                        nav.navigate(Routes.hist_cra)
                    }
                    else -> {
                        nav.navigate(Routes.cra_fields)
                    }
                }
            }) {
                viewModel.clearHistoricalChoice()
                nav.popBackStack()
            }
        }
        b.composable(Routes.hist_cra) {
            HistoricalCRA.Screen(viewModel, snack, {
                nav.navigate(Routes.cra_fields)
            }) {
                nav.popBackStack()
            }
        }
        b.composable(Routes.cra_fields) {
            CRAFields.Screen(viewModel, snack, {
                viewModel.clear()
                nav.popClear(Analysis.Routes.dashboard)
            }) {
                nav.popBackStack()
            }
        }
    }

    @Composable
    fun Screen(viewModel: CraViewModel, temporal: String, snack: SnackFun, next: Click, back: Click, previous: String?, setLocal: (CRAFile) -> Unit, setRemote: (String) -> Unit) {
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
                Effect.Once {
                    snack(remoteCRAs.exceptionOrNull()!!.message!!)
                    back()
                }
            }
            else -> {
                SelectCRA(viewModel, temporal, remoteCRAs.getOrNull()!!, snack, next, previous, setLocal, setRemote)
            }
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun SelectCRA(viewModel: CraViewModel, temporal: String, remoteCRAs: List<String>, snack: SnackFun, next: Click, previous: String?, setLocal: (CRAFile) -> Unit, setRemote: (String) -> Unit) {
        val (selectedFiles, setSelectedFiles) = remember { mutableStateOf<List<Uri>?>(null) }
        val (validating, setValidating) = remember { mutableStateOf(false) }
        val (localCRA, setLocalCRA) = remember { mutableStateOf<Result<CRAFile>?>(null) }

        if (validating) {
            Progress()
            when {
                localCRA == null -> {
                    val streams = mutableListOf<InputStream?>()
                    val names = mutableListOf<String?>()
                    selectedFiles?.forEach { uri ->
                        streams.add(LocalContext.current.contentResolver.openInputStream(uri))
                        names.add(contentDisplayName(LocalContext.current, uri))
                    }
                    viewModel.validateLocalCRA(streams, names, remoteCRAs, previous, setLocalCRA)
                }
                localCRA.isFailure -> {
                    val e = localCRA.exceptionOrNull()!!
                    LaunchedEffect(key1 = e) {
                        snack(e.message!!)
                        setLocalCRA(null)
                        setSelectedFiles(null)
                        setValidating(false)
                    }
                }
                localCRA.isSuccess -> {
                    val cra = localCRA.getOrNull()!!
                    LaunchedEffect(key1 = cra) {
                        setLocal(cra)
                        setLocalCRA(null)
                        setSelectedFiles(null)
                        setValidating(false)
                        next()
                    }
                }
            }
        } else {
            val remotes = remoteCRAs.toMutableList()
            if (previous != null && remotes.contains(previous)) {
                remotes.remove(previous)
            }

            Selection(temporal, remotes, setSelectedFiles, setValidating, next, setRemote)
        }
    }

    @Composable
    fun Selection(temporal: String, remoteCRAs: List<String>, setSelectedFiles: (List<Uri>?) -> Unit, setValidating: (Boolean) -> Unit, next: Click, setRemote: (String) -> Unit) {
        Col.Between {
            if (remoteCRAs.isEmpty()) {
                LocalCRA(temporal, setSelectedFiles, setValidating)
            } else {
                LocalRemoteSwitch(temporal, remoteCRAs, setSelectedFiles, setValidating, next, setRemote)
            }
        }
    }

    @Composable
    fun LocalRemoteSwitch(temporal: String, remoteCRAs: List<String>, setSelectedFiles: (List<Uri>?) -> Unit, setValidating: (Boolean) -> Unit, next: Click, setRemote: (String) -> Unit) {
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
            RemoteCRA(temporal, remoteCRAs, next, setRemote)
        } else {
            LocalCRA(temporal, setSelectedFiles, setValidating)
        }
    }

    @Composable
    fun LocalCRA(temporal: String, setSelectedFiles: (List<Uri>?) -> Unit, setValidating: (Boolean) -> Unit) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { files ->
            setValidating(true)
            setSelectedFiles(files)
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
        Spacer(modifier = Modifier.height(0.dp))
        Butt.Text("Select Shapefile") {
            launcher.launch(arrayOf("*/*"))
        }
    }

    @Composable
    fun RemoteCRA(temporal: String, remoteCRAs: List<String>, next: Click, setRemote: (String) -> Unit) {
        val nextEnabled = remember { mutableStateOf(false) }
        val selected = remember { mutableStateOf("") }
        Dropdown(title = "Select $temporal Shapefile:", labels = remoteCRAs) { i ->
            selected.value = remoteCRAs[i]
            nextEnabled.value = true
        }

        Butt.Next(nextEnabled.value) {
            setRemote(selected.value)
            next()
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
}