package org.blueventures.gemdroid.ui.common.maps

import android.content.Context
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.GetRemote
import java.io.File
import java.net.HttpURLConnection

object Visualize {
    interface Visualizer {
        fun parentDir(): File
        fun tileDir(i: Int): File
        fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit): Job
        fun getVisualizeURLs(callback: (ApiResult<VisualizeURLs>) -> Unit)
        fun saveVisualizeURLsFile(urls: VisualizeURLs): Job
    }

    @Composable
    fun Screen(
        visualizer: Visualizer?,
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        center: LatLng? = null,
        storage: Maps.Storage? = null,
        draw: Draw.Model? = null,
        poly: Poly.Model? = null,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        if (visualizer != null) {
            GetRemote.Save(visualizer::loadVisualizeURLsFile, visualizer::getVisualizeURLs, visualizer::saveVisualizeURLsFile, false, Visualize::errHandler) { urls ->
                Maps.Screen(appBar, title, attemptGps, center, storage = storage, layers = object : Layers.Model<VisualizeURLs>() {
                    override val initUrls = urls
                    override val layerNames = stringArrayResource(R.array.false_color_layers).toList()
                    override val parentDir = visualizer.parentDir()

                    override fun tileDir(i: Int) = visualizer.tileDir(i)
                    override fun getRemote(callback: (ApiResult<VisualizeURLs>) -> Unit) = visualizer.getVisualizeURLs(callback)
                    override fun save(urls: VisualizeURLs) = visualizer.saveVisualizeURLsFile(urls)
                }, draw = draw, poly = poly, floating = floating)
            }
        } else {
            Maps.NoLayers(appBar, title, attemptGps, center, storage = storage, draw = draw, poly = poly, floating = floating)
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
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
        }
        
        return Pair(null, true)
    }
}