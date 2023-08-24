package org.blueventures.gemdroid.ui.common.maps

import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.tiles.CachingUrlTileProvider
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Effect
import java.io.File

object Tiles {
    abstract class Model<T> {
        abstract val initUrls: T
        abstract val parentDir: File
        abstract val bounds: List<LatLng>

        abstract fun url(i: Int, urls: T): String
        abstract fun tileDir(i: Int): File
        abstract fun staleCheck(urls: T): Boolean
        abstract fun getRemote(callback: (ApiResult<T>) -> Unit)
        abstract fun save(urls: T): Job

        fun tileOpts(index: Int, urls: T): TileOverlayOptions {
            return TileOverlayOptions().tileProvider(
                CachingUrlTileProvider(
                    parentDir,
                    tileDir(index),
                    url(index, urls),
                )
            ).zIndex((layers.size - index).toFloat())
        }
    }

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

    interface UrlHandler<T> {
        fun getUrls(): T
        fun setUrls(urls: T)
    }

    @Composable
    fun <T> Setup(
        title: String,
        appBar: AppBarFun,
        tileModel: Model<T>?,
        content: @Composable (UrlHandler<T>?) -> Unit
    ) {
        var refreshedURLs by remember { mutableStateOf(false) }
        val (urls, setUrls) = remember { mutableStateOf(tileModel?.initUrls) }

        tileModel?.let { tiles ->
            if (tiles.staleCheck(urls!!) && !refreshedURLs) {
                tiles.getRemote { result ->
                    if (result is ApiResult.Success) {
                        val data = result.data!!
                        tiles.save(data)
                        setUrls(data)
                        refreshedURLs = true
                    }
                }
            }

            layers.forEach { layer ->
                var checked by remember { mutableStateOf(true) }
                layer.getChecked = { checked }
                layer.setChecked = { checked = it }
            }

            Effect.Once {
                appBar(AppBarUpdate(
                    title = title,
                    actions = { LayersDropdown(*layers.toTypedArray()) }
                ))
            }

            content(object: UrlHandler<T> {
                override fun getUrls() = urls!!
                override fun setUrls(urls: T) = setUrls(urls)
            })
        } ?: run {
            if (title.isNotEmpty()) {
                Effect.Once {
                    appBar(AppBarUpdate(title = title))
                }
            }

            content(null)
        }
    }

    class MapCallback<T>(private val model: Model<T>, private val urls: UrlHandler<T>): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            layers.forEachIndexed { i, layer ->
                layer.overlay = map.addTileOverlay(model.tileOpts(i, urls.getUrls()))
            }
        }
    }

    val layers: MutableList<Layer> = mutableListOf()

    @Composable
    fun LayerSetup(@StringRes vararg titles: Int) {
        layers.clear()
        for (id in titles) {
            layers.add(Layer(stringResource(id)))
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
}