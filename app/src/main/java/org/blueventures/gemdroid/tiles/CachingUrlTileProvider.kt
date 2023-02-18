package org.blueventures.gemdroid.tiles

import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.UrlTileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URL

class CachingUrlTileProvider(
    private val tileDir: File,
    private val baseUrl: String,
    private val width: Int,
    private val height: Int,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): TileProvider {
    private val urlProvider: UrlTileProvider = UrlProvider()

    override fun getTile(x: Int, y: Int, z: Int): Tile? = runBlocking {
        val sep = FileService.sep
        val cachedFile = File(tileDir, "$z$sep$x$sep$y")

        val deferred = async(ioDispatcher) {
            FileService.readFile(cachedFile)
        }

        val tile: Tile?
        val cachedTile = deferred.await()
        if (cachedTile == null) {
            tile = urlProvider.getTile(x, y, z)
            tile?.let {
                it.data?.let { img ->
                    launch(ioDispatcher) {
                        FileService.createDir(tileDir, "$z$sep$x")?.let { dir ->
                            FileService.writeFile(File(dir, "$y"), img)
                        }
                    }
                }
            }
        } else {
            tile = Tile(width, height, cachedTile)
        }

        tile
    }

    private inner class UrlProvider: UrlTileProvider(width, height) {
        override fun getTileUrl(x: Int, y: Int, z: Int): URL {
            return URL(baseUrl.replace(zPlaceholder, z.toString()).replace(xPlaceholder, x.toString()).replace(yPlaceholder, y.toString()))
        }
    }

    companion object {
        const val zPlaceholder = "{z}"
        const val xPlaceholder = "{x}"
        const val yPlaceholder = "{y}"
    }
}