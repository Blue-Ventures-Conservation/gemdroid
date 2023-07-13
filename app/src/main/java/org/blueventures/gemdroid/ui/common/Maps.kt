package org.blueventures.gemdroid.ui.common

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
import org.blueventures.gemdroid.databinding.MapContainerBinding
import org.blueventures.gemdroid.tiles.CachingUrlTileProvider
import java.io.File

object Maps {
    /**
     * A Layer represents a TileOverlay that is meant to be toggleable in a dropdown.
     */
    class Layer(val title: String) {
        var overlay: TileOverlay? = null

        var getChecked: () -> Boolean = { true }
        var setChecked: (Boolean) -> Unit = {}

        fun checked() = getChecked()
        fun check(value: Boolean) {
            overlay?.isVisible = value
            setChecked(value)
        }
    }

    /**
     * The layers argument to Screen should be Layers that outlive composition, such as static fields on an object for example.
     */
    @Composable
    fun <T> Screen(
        title: String,
        initURLs: T,
        roiDir: File,
        appBar: AppBarFun,
        staleCheck: (T) -> Boolean,
        tileDir: (Int) -> File,
        url: (Int, T) -> String,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        save: (T) -> Unit,
        bounds: List<List<Double>>,
        vararg layers: Layer) {
        Effect.Once {
            appBar(AppBarUpdate(
                title = title,
                actions = {
                    LayersDropdown(*layers)
                }
            ))
        }

        var refreshedURLs by remember { mutableStateOf(false) }
        var urls by remember { mutableStateOf(initURLs) }

        if (staleCheck(urls) && !refreshedURLs) {
            getRemote { result ->
                if (result is ApiResult.Success) {
                    val data = result.data!!
                    save(data)
                    urls = data
                    refreshedURLs = true
                }
            }
        }

        layers.forEach { layer ->
            var checked by remember { mutableStateOf(true) }
            layer.getChecked = { checked }
            layer.setChecked = { checked = it }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidViewBinding(MapContainerBinding::inflate) {
                val mapFragment = mapContainer.getFragment<SupportMapFragment>()
                mapFragment.getMapAsync { map ->
                    map.clear()

                    layers.forEachIndexed { i, layer ->
                        layer.overlay = map.addTileOverlay(tileOpts(roiDir, tileDir(i), url(i, urls), layers.size - i))
                    }

                    val builder = LatLngBounds.builder()
                    for (pt in bounds) {
                        builder.include(LatLng(pt[1], pt[0]))
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
                }
            }
        }
    }

    @Composable
    private fun LayersDropdown(vararg layers: Layer) {
        val (menu, setMenu) = remember { mutableStateOf(false) }
        IconButton(onClick = { setMenu(!menu) }) {
            Icon(Icons.Filled.MoreVert, "")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }) {
            layers.forEach { layer ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Checkbox(checked = layer.checked(), onCheckedChange = layer::check)
                    Text(layer.title, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
    }

    private fun tileOpts(roiDir: File, tileDir: File, url: String, zIndex: Int): TileOverlayOptions {
        return TileOverlayOptions().tileProvider(
            CachingUrlTileProvider(
                roiDir,
                tileDir,
                url,
            )
        ).zIndex(zIndex.toFloat())
    }
}