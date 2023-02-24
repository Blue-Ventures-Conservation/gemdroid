package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.PleaseWait
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.RefreshableError
import java.io.File

object Visualize {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, setAppBarState: (AppBarUpdate) -> Unit, snackbar: (String) -> Unit, backClick: () -> Unit) {
        setAppBarState(AppBarUpdate(title = "Visualize Imagery"))
        viewModel.clearStage()
        viewModel.clearBuffers()
        Visualize(viewModel, setAppBarState, snackbar, backClick)
    }

    @Composable
    fun Visualize(viewModel: AnalysisViewModel, setAppBarState: (AppBarUpdate) -> Unit, snackbar: (String) -> Unit, backClick: () -> Unit) {
        val state by viewModel.state.collectAsState()

        when {
            state.roi == null -> {
                Progress()
                viewModel.getROI()
            }
            state.roi!!.isFailure -> {
                LaunchedEffect(key1 = true) {
                    snackbar(state.roi!!.exceptionOrNull()!!.message!!)
                    backClick()
                }
            }
            state.visualizeURLs == null -> {
                Progress()
                viewModel.loadVisualizeURLs()
            }
            !VisualizeURLs.isEmpty(state.visualizeURLs!!) -> {
                VisualizeMap(viewModel, state.visualizeURLs!!, setAppBarState)
            }
            state.visualizeURLsResult == null -> {
                PleaseWait()
                state.roi!!.getOrNull()?.let {
                    viewModel.getVisualizeURLs(it)
                }
            }
            state.visualizeURLsResult is ApiResult.Error -> {
                RefreshableError(state.roi!!.getOrNull()!!) { roi, callback ->
                    viewModel.getVisualizeURLs(roi, callback)
                }
            }
            state.visualizeURLsResult is ApiResult.Success -> {
                val urls = state.visualizeURLsResult!!.data!!
                viewModel.saveVisualizeURLs(urls)
                VisualizeMap(viewModel, urls, setAppBarState)
            }
        }

        BackHandler {
            backClick()
        }
    }

    private fun chotCheck(check: Boolean) = chotCheckCall(check)
    private var chotCheckCall: ((Boolean) -> Unit) = {}
    private fun clotCheck(check: Boolean) = clotCheckCall(check)
    private var clotCheckCall: ((Boolean) -> Unit) = {}
    private fun hhotCheck(check: Boolean) = hhotCheckCall(check)
    private var hhotCheckCall: ((Boolean) -> Unit) = {}
    private fun hlotCheck(check: Boolean) = hlotCheckCall(check)
    private var hlotCheckCall: ((Boolean) -> Unit) = {}
    private fun chotChecked() = chotCheckedCall()
    private var chotCheckedCall: (() -> Boolean) = { true }
    private fun clotChecked() = clotCheckedCall()
    private var clotCheckedCall: (() -> Boolean) = { true }
    private fun hhotChecked() = hhotCheckedCall()
    private var hhotCheckedCall: (() -> Boolean) = { true }
    private fun hlotChecked() = hlotCheckedCall()
    private var hlotCheckedCall: (() -> Boolean) = { true }

    @Composable
    fun VisualizeMap(viewModel: AnalysisViewModel, urls: VisualizeURLs, setAppBarState: (AppBarUpdate) -> Unit) {
        LaunchedEffect(key1 = true) {
            setAppBarState(AppBarUpdate(
                title = "VisualizeImagery",
                actions = {
                    MapVisualizeDropDown(
                        Visualize::chotChecked, Visualize::chotCheck,
                        Visualize::clotChecked, Visualize::clotCheck,
                        Visualize::hhotChecked, Visualize::hhotCheck,
                        Visualize::hlotChecked, Visualize::hlotCheck
                    )
                }
            ))
        }

        var chot: TileOverlay? = null
        var clot: TileOverlay? = null
        var hhot: TileOverlay? = null
        var hlot: TileOverlay? = null
        var isChotChecked by remember { mutableStateOf(true) }
        var isClotChecked by remember { mutableStateOf(true) }
        var isHhotChecked by remember { mutableStateOf(true) }
        var isHlotChecked by remember { mutableStateOf(true) }
        chotCheckCall = {
            chot?.isVisible = it
            isChotChecked = it
        }
        chotCheckedCall = {
            isChotChecked
        }
        clotCheckCall = {
            clot?.isVisible = it
            isClotChecked = it
        }
        clotCheckedCall = {
            isClotChecked
        }
        hhotCheckCall = {
            hhot?.isVisible = it
            isHhotChecked = it
        }
        hhotCheckedCall = {
            isHhotChecked
        }
        hlotCheckCall = {
            hlot?.isVisible = it
            isHlotChecked = it
        }
        hlotCheckedCall = {
            isHlotChecked
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidViewBinding(MapContainerBinding::inflate) {
                val mapFragment = mapContainer.getFragment<SupportMapFragment>()
                mapFragment.getMapAsync { map ->
                    map.clear()
                    val roiDir = viewModel.state.value.roiDir
                    chot = map.addTileOverlay(tileOpts(roiDir, viewModel.chotTileDir(), urls.chotURL, 4f))
                    clot = map.addTileOverlay(tileOpts(roiDir, viewModel.clotTileDir(), urls.clotURL, 3f))
                    hhot = map.addTileOverlay(tileOpts(roiDir, viewModel.hhotTileDir(), urls.hhotURL, 2f))
                    hlot = map.addTileOverlay(tileOpts(roiDir, viewModel.hlotTileDir(), urls.hlotURL, 1f))

                    val builder = LatLngBounds.builder()
                    for (pt in viewModel.state.value.roi!!.getOrNull()!!.polygon.coordinates[0]) {
                        builder.include(LatLng(pt[1], pt[0]))
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
                }
            }
        }
    }

    @Composable
    fun MapVisualizeDropDown(chotChecked: () -> Boolean, chotCheck: (Boolean) -> Unit, clotChecked: () -> Boolean, clotCheck: (Boolean) -> Unit, hhotChecked: () -> Boolean, hhotCheck: (Boolean) -> Unit, hlotChecked: () -> Boolean, hlotCheck: (Boolean) -> Unit) {
        val (menu, setMenu) = remember { mutableStateOf(false) }
        IconButton(onClick = { setMenu(!menu) }) {
            Icon(Icons.Filled.MoreVert, "")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }) {
            MapVisualizeMenuItem("Contemporary High Tide", chotChecked, chotCheck)
            MapVisualizeMenuItem("Contemporary Low Tide", clotChecked, clotCheck)
            MapVisualizeMenuItem("Historical High Tide", hhotChecked, hhotCheck)
            MapVisualizeMenuItem("Historical Low Tide", hlotChecked, hlotCheck)
        }
    }

    @Composable
    fun MapVisualizeMenuItem(title: String, checked: () -> Boolean, onCheck: (Boolean) -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(checked = checked(), onCheckedChange = onCheck)
            Text(title, modifier = Modifier.padding(end = 8.dp))
        }
    }

    private fun tileOpts(roiDir: File, tileDir: File, url: String, zIndex: Float): TileOverlayOptions {
        return TileOverlayOptions().tileProvider(
            CachingUrlTileProvider(
                roiDir,
                tileDir,
                url,
                256,
                256,
            )
        ).zIndex(zIndex)
    }
}