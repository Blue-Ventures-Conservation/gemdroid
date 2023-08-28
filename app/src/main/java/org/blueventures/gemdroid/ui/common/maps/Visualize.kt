package org.blueventures.gemdroid.ui.common.maps

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.File

object Visualize {
    interface Visualizer {
        fun parentDir(): File
        fun bounds(): List<LatLng>
        fun tileDir(i: Int): File
        fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit): Job
        fun getVisualizeURLs(callback: (ApiResult<VisualizeURLs>) -> Unit)
        fun saveVisualizeURLsFile(urls: VisualizeURLs): Job
    }

    @Composable
    fun Screen(title: String, visualizer: Visualizer, appBar: AppBarFun, snack: SnackFun = {}, back: Click = {}, next: Click = {}, floatingContent: @Composable BoxScope.() -> Unit = {}, draw: Draw.Model? = null, poly: Poly.Model? = null) {
        Tiles.LayerSetup(R.string.cont_high_tide, R.string.cont_low_tide, R.string.hist_high_tide, R.string.hist_low_tide)

        GetRemote.Save(visualizer::loadVisualizeURLsFile, visualizer::getVisualizeURLs, visualizer::saveVisualizeURLsFile) { urls ->
            Maps.Screen(title, appBar, snack, back, next, floatingContent, false, object : Tiles.Model<VisualizeURLs>() {
                override val initUrls = urls
                override val parentDir = visualizer.parentDir()
                override val bounds = visualizer.bounds()

                override fun url(i: Int, urls: VisualizeURLs) = urls.ordered(i)
                override fun tileDir(i: Int) = visualizer.tileDir(i)
                override fun getRemote(callback: (ApiResult<VisualizeURLs>) -> Unit) = visualizer.getVisualizeURLs(callback)
                override fun save(urls: VisualizeURLs) = visualizer.saveVisualizeURLsFile(urls)
            }, draw, poly)
        }
    }
}