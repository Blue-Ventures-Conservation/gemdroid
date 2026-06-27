package org.blueventures.gemdroid.ui.common.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.TileOverlay
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.tiles.CachingUrlTileProvider
import org.blueventures.gemdroid.ui.common.maps.Maps.Checker
import java.io.File

object Layers {
    abstract class Model<T : URLs> {
        abstract val initUrls: T
        abstract val layerNames: List<String>
        abstract val parentDir: File

        abstract fun tileDir(i: Int): File
        abstract fun getRemote(callback: (ApiResult<T>) -> Unit)
        abstract fun save(urls: T, callback: (Result<Unit>) -> Unit)

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

    fun <T : URLs> checkers(model: Model<T>, checkers: MutableList<Checker>) {
        model.layerNames.forEach { name ->
            checkers.add(Checker(name))
        }
    }

    @Composable
    @GoogleMapComposable
    fun <T : URLs> Display(model: Model<T>, checkers: List<Checker>) {
        val (urls, setUrls) = remember { mutableStateOf(model.initUrls) }
        if (staleCheck(urls)) {
            model.getRemote { result ->
                if (result is ApiResult.Success) {
                    val data = result.data!!
                    model.save(data) {}
                    setUrls(data)
                }
            }
        }

        model.layerNames.forEachIndexed { index, layer ->
            var chk: Checker? = null
            for (c in checkers) {
                if (c.name == layer) {
                    chk = c
                    break
                }
            }

            chk?.let { checker ->
                val opts = model.tileOpts(index, urls)
                opts.tileProvider?.let { provider ->
                    Overlay(provider, checker, opts)
                }
            }
        }
    }

    @Composable
    @GoogleMapComposable
    // this function prevents layers from flickering when toggled
    // by separating their contexts from each other
    private fun Overlay(provider: TileProvider, checker: Checker, opts: TileOverlayOptions) {
        val (visible, setVisible) = remember { mutableStateOf(true) }
        checker.state = visible
        checker.setState = setVisible
        TileOverlay(provider, visible = checker.state, zIndex = opts.zIndex)
    }
}