package org.blueventures.gemdroid.ui.common.maps

import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.tiles.CachingUrlTileProvider
import org.blueventures.gemdroid.ui.common.AppBar
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
    }
}