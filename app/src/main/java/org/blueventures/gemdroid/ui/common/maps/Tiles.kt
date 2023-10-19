package org.blueventures.gemdroid.ui.common.maps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.tiles.CachingUrlTileProvider
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import java.io.File

object Tiles {
    abstract class Model<T : URLs> {
        abstract val title: String
        abstract val appBar: AppBar
        abstract val initUrls: T
        abstract val layerNames: List<String>
        abstract val parentDir: File
        abstract val bounds: List<LatLng>

        abstract fun tileDir(i: Int): File
        abstract fun getRemote(callback: (ApiResult<T>) -> Unit)
        abstract fun save(urls: T): Job

        fun tileOpts(index: Int, urls: T): TileOverlayOptions {
            return TileOverlayOptions().tileProvider(
                CachingUrlTileProvider(
                    parentDir,
                    tileDir(index),
                    urls.ordered(index),
                )
            ).zIndex((layerNames.size - index).toFloat())
        }

        @Composable
        fun setup(
            polygons: Layer? = null,
        ): Handler<T> {
            val layerState = mutableListOf<Layer>()
            for (name in layerNames) {
                layerState.add(TileLayer(name))
            }

            if (polygons != null) {
                layerState.add(polygons)
            }

            val layers = remember { mutableStateOf(layerState.toList()) }
            val pairs = mutableListOf<Pair<String, Boolean>>()
            for (layer in layers.value) {
                pairs.add(Pair(layer.title, true))
            }

            val checkMap = remember { mutableStateMapOf(*pairs.toTypedArray()) }

            appBar.Update(AppBarUpdate(
                title = title,
                actions = { LayersDropdown(layers.value, checkMap) }
            ))

            val (urls, setUrls) = remember { mutableStateOf(initUrls) }

            if (staleCheck(urls)) {
                getRemote { result ->
                    if (result is ApiResult.Success) {
                        val data = result.data!!
                        save(data)
                        setUrls(data)
                    }
                }
            }

            return object : Handler<T> {
                override val urls: T = urls
                override val layers: MutableList<Layer> = layers.value.toMutableList()
            }
        }
    }

    /**
     * A Layer represents a TileOverlay that is meant to be toggleable in a dropdown.
     */
    abstract class Layer(val title: String) {
        abstract fun toggle(checked: Boolean)
    }

    class TileLayer(title: String): Layer(title) {
        var overlay: TileOverlay? = null
        override fun toggle(checked: Boolean) {
            overlay?.isVisible = checked
        }
    }

    interface Handler<T> {
        val urls: T
        val layers: MutableList<Layer>
    }

    class MapCallback<T : URLs>(private val model: Model<T>, private val handler: Handler<T>): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            handler.layers.forEachIndexed { i, layer ->
                (layer as? TileLayer)?.let {
                    val old = layer.overlay
                    layer.overlay = map.addTileOverlay(model.tileOpts(i, handler.urls))
                    old?.remove()
                }
            }
        }
    }

    @Composable
    private fun LayersDropdown(layers: List<Layer>, checkMap: SnapshotStateMap<String, Boolean>) {
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
                    Checkbox(checkMap[layer.title]!!, onCheckedChange = {
                        layer.toggle(it)
                        checkMap[layer.title] = it
                    })
                    Text(layer.title, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
    }
}