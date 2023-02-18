package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.databinding.MapContainerBinding
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.tiles.CachingUrlTileProvider
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.RefreshableError
import java.io.File

object Visualize {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, snackbar: (String) -> Unit, backClick: () -> Unit) {
        viewModel.clearStage()
        viewModel.clearBuffers()
        Visualize(viewModel, snackbar, backClick)
    }

    @Composable
    fun Visualize(viewModel: AnalysisViewModel, snackbar: (String) -> Unit, backClick: () -> Unit) {
        val state by viewModel.state.collectAsState()

        when {
            state.roi == null -> {
                Progress()
                viewModel.getROI()
            }
            state.roi!!.isFailure -> {
                snackbar(state.roi!!.toString())
                backClick()
            }
            state.visualizeURLs == null -> {
                Progress()
                viewModel.loadVisualizeURLs()
            }
            !VisualizeURLs.isEmpty(state.visualizeURLs!!) -> {
                VisualizeMap(viewModel, state.visualizeURLs!!, snackbar)
            }
            state.visualizeURLsResult == null -> {
                PleaseWait()
                state.roi!!.getOrNull()?.let {
                    viewModel.getVisualizeURLs(it)
                }
            }
            state.visualizeURLsResult is ApiResult.Error -> {
                RefreshableError("Visualize Imagery", state.roi!!.getOrNull()!!) { roi, callback ->
                    viewModel.getVisualizeURLs(roi, callback)
                }
            }
            state.visualizeURLsResult is ApiResult.Success -> {
                val urls = state.visualizeURLsResult!!.data!!
                viewModel.saveVisualizeURLs(urls)
                VisualizeMap(viewModel, urls, snackbar)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun VisualizeMap(viewModel: AnalysisViewModel, urls: VisualizeURLs, snackbar: (String) -> Unit) {
        var chot: TileOverlay? = null
        var clot: TileOverlay? = null
        var hhot: TileOverlay? = null
        var hlot: TileOverlay? = null

        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text("Visualize Imagery") },
                    actions = {
                        MapVisualizeDropDown(
                            chotCheck = {
                                chot?.isVisible = it
                        },clotCheck = {
                            clot?.isVisible = it
                        }, hhotCheck = {
                            hhot?.isVisible = it
                        }, hlotCheck = {
                            hlot?.isVisible = it
                        })
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                AndroidViewBinding(MapContainerBinding::inflate) {
                    val mapFragment = mapContainer.getFragment<SupportMapFragment>()
                    mapFragment.getMapAsync { map ->
                        map.clear()
                        chot = map.addTileOverlay(tileOpts(viewModel.chotTileDir(), urls.chotURL, 4f))
                        clot = map.addTileOverlay(tileOpts(viewModel.clotTileDir(), urls.clotURL, 3f))
                        hhot = map.addTileOverlay(tileOpts(viewModel.hhotTileDir(), urls.hhotURL, 2f))
                        hlot = map.addTileOverlay(tileOpts(viewModel.hlotTileDir(), urls.hlotURL, 1f))

                        val builder = LatLngBounds.builder()
                        for (pt in viewModel.state.value.roi!!.getOrNull()!!.polygon.coordinates[0]) {
                            builder.include(LatLng(pt[1], pt[0]))
                        }
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
                    }
                }
            }
        }
    }

    @Composable
    fun MapVisualizeDropDown(chotCheck: (Boolean) -> Unit, clotCheck: (Boolean) -> Unit, hhotCheck: (Boolean) -> Unit, hlotCheck: (Boolean) -> Unit) {
        val (menu, setMenu) = remember { mutableStateOf(false) }
        var chotChecked by remember { mutableStateOf(true) }
        var clotChecked by remember { mutableStateOf(true) }
        var hhotChecked by remember { mutableStateOf(true) }
        var hlotChecked by remember { mutableStateOf(true) }
        IconButton(onClick = { setMenu(!menu) }) {
            Icon(Icons.Filled.MoreVert, "")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }) {
            MapVisualizeMenuItem("Contemporary High Tide", chotChecked) {
                chotChecked = it
                chotCheck(it)
            }
            MapVisualizeMenuItem("Contemporary Low Tide", clotChecked) {
                clotChecked = it
                clotCheck(it)
            }
            MapVisualizeMenuItem("Historical High Tide", hhotChecked) {
                hhotChecked = it
                hhotCheck(it)
            }
            MapVisualizeMenuItem("Historical Low Tide", hlotChecked) {
                hlotChecked = it
                hlotCheck(it)
            }
        }
    }

    @Composable
    fun MapVisualizeMenuItem(title: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheck)
            Text(title, modifier = Modifier.padding(end = 8.dp))
        }
    }

    private fun tileOpts(tileDir: File, url: String, zIndex: Float): TileOverlayOptions {
        return TileOverlayOptions().tileProvider(
            CachingUrlTileProvider(
                tileDir,
                url,
                256,
                256,
            )
        ).zIndex(zIndex)
    }
}