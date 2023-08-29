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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import java.io.File

object Tiles {
    abstract class Model<T : URLs> {
        abstract val title: String
        abstract val appBar: AppBarFun
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
    }

    /**
     * A Layer represents a TileOverlay that is meant to be toggleable in a dropdown.
     */
    class Layer(val title: String) {
        var overlay: TileOverlay? = null
    }

    interface Handler<T> {
        val urls: T
        val layers: List<Layer>
    }

    @Composable
    fun <T : URLs> Setup(
        model: Model<T>?,
        content: @Composable (Handler<T>?) -> Unit
    ) {
        model?.let { tiles ->
            val layers = remember { mutableStateOf(tiles.layerNames.map { Layer(it) }) }

            tiles.appBar(AppBarUpdate(
                title = tiles.title,
                actions = { LayersDropdown(layers.value) }
            ))

            val (urls, setUrls) = remember { mutableStateOf(model.initUrls) }

            if (staleCheck(urls)) {
                tiles.getRemote { result ->
                    if (result is ApiResult.Success) {
                        val data = result.data!!
                        tiles.save(data)
                        setUrls(data)
                    }
                }
            }

            content(object: Handler<T> {
                override val urls: T = urls
                override val layers: List<Layer> = layers.value
            })
        } ?: run {
            content(null)
        }
    }

    class MapCallback<T : URLs>(private val model: Model<T>, private val handler: Handler<T>): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            handler.layers.forEachIndexed { i, layer ->
                val old = layer.overlay
                layer.overlay = map.addTileOverlay(model.tileOpts(i, handler.urls))
                old?.remove()
            }
        }
    }

    @Composable
    private fun LayersDropdown(layers: List<Layer>) {
        val (menu, setMenu) = remember { mutableStateOf(false) }
        IconButton(onClick = { setMenu(!menu) }) {
            Icon(Icons.Filled.MoreVert, "")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }) {
            layers.forEach { layer ->
                val (checked, setChecked) = remember { mutableStateOf(true) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Checkbox(checked = checked, onCheckedChange = {
                        layer.overlay?.isVisible = it
                        setChecked(it)
                    })
                    Text(layer.title, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
    }
}