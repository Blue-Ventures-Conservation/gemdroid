package org.blueventures.gemdroid.ui.common.maps

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringArrayResource
import com.google.android.gms.maps.model.LatLngBounds
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.maps.Maps.FloatingNext
import java.io.File

object Visualize {
    interface Visualizer {
        fun parentDir(): File
        fun tileDir(i: Int): File
        fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit)
        fun getVisualizeURLs(callback: (Result<VisualizeURLs>) -> Unit)
        fun saveVisualizeURLsFile(urls: VisualizeURLs, callback: (Result<Unit>) -> Unit)
        fun aofPolygonGroup(context: Context, aof: GeojsonMultiPolygon, callback: (Polygons.Group) -> Unit)
    }

    interface BoundsGetter {
        fun getInitialLatLngBounds(callback: (Result<LatLngBounds>) -> Unit)
    }

    @Composable
    fun Screen(
        visualizer: Visualizer?,
        boundsGetter: BoundsGetter,
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        storage: Maps.Storage? = null,
        poly: Polygons.Model? = null,
        capture: Capture.Model? = null,
        draw: Draw.Model? = null,
        next: FloatingNext? = null,
    ) {
        val (boundsResult, setBoundsResult) = remember { mutableStateOf<Result<LatLngBounds?>?>(null) }
        when {
            boundsResult == null -> boundsGetter.getInitialLatLngBounds(setBoundsResult)
            else -> {
                if (visualizer != null) {
                    GetRemote.Save(visualizer::loadVisualizeURLsFile, visualizer::getVisualizeURLs, visualizer::saveVisualizeURLsFile, false, Visualize::errHandler) { urls ->
                        Maps.Screen(appBar, title, attemptGps, storage, boundsResult.getOrNull(), areaOfFocusPolygonsModel(visualizer, urls, poly),
                            capture, object : Layers.Model<VisualizeURLs>() {
                                override val initUrls = urls
                                override val layerNames = stringArrayResource(R.array.false_color_layers).toList()
                                override val parentDir = visualizer.parentDir()

                                override fun tileDir(i: Int) = visualizer.tileDir(i)
                                override fun getRemote(callback: (Result<VisualizeURLs>) -> Unit) = visualizer.getVisualizeURLs(callback)
                                override fun save(urls: VisualizeURLs, callback: (Result<Unit>) -> Unit) = visualizer.saveVisualizeURLsFile(urls, callback)
                            }, draw, next)
                    }
                } else {
                    Maps.NoLayers(appBar, title, attemptGps, storage, boundsResult.getOrNull(), poly, capture, draw, next)
                }
            }
        }
    }

    private fun areaOfFocusPolygonsModel(visualizer: Visualizer, urls: VisualizeURLs, poly: Polygons.Model?): Polygons.Model {
        return object: Polygons.Model() {
            override val touchEnabled: Boolean = poly?.touchEnabled ?: true
            override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) {
                poly?.polygonGroups(context) { groups ->
                    urls.areaOfFocus?.let {
                        visualizer.aofPolygonGroup(context, urls.areaOfFocus) { group ->
                            val list = groups.toMutableList()
                            list.add(group)
                            callback(list)
                        }
                    } ?: run {
                        callback(groups)
                    }
                } ?: run {
                    urls.areaOfFocus?.let {
                        visualizer.aofPolygonGroup(context, urls.areaOfFocus) { group ->
                            callback(listOf(group))
                        }
                    } ?: run {
                        visualizer.loadVisualizeURLsFile {
                            callback(emptyList())
                        }
                    }
                }
            }
            override fun onTouch(point: Polygons.PolygonPoint?) = @Composable {
                if (poly != null) {
                    poly.onTouch(point).invoke()
                } else {
                    Polygons.PlaceMarker(point)
                    false
                }
            }
        }
    }

    fun errHandler(ctx: Context, message: String?): Pair<String?, Boolean> {
        message?.let { msg ->
            return when {
                msg.contains("no historical images") -> {
                    Pair(ctx.getString(R.string.too_few_historical_images), false)
                }
                msg.contains("no contemporary images") -> {
                    Pair(ctx.getString(R.string.too_few_contemporary_images), false)
                }
                else -> Pair(null, true)
            }
        }

        return Pair(null, true)
    }

    @Composable
    fun Capture(
        visualizer: Visualizer?,
        boundsGetter: BoundsGetter,
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = true,
        storage: Maps.Storage? = null,
        poly: Polygons.Model? = null,
        capture: Capture.Model? = null,
    ) {
        Screen(visualizer, boundsGetter, appBar, title, attemptGps, storage, poly, capture, null)
    }
}