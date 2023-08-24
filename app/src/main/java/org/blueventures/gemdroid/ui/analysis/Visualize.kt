package org.blueventures.gemdroid.ui.analysis

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.StaleCheck
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Tiles

object Visualize {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, back: Click) {
        Tiles.LayerSetup(R.string.cont_high_tide, R.string.cont_low_tide, R.string.hist_high_tide, R.string.hist_low_tide)

        GetRemote.Save(viewModel::loadVisualizeURLsFile, viewModel::getVisualizeURLs, viewModel::saveVisualizeURLsFile) { urls ->
            Maps.Screen(stringResource(R.string.visualize_imagery_title), appBar, back = back, tiles = object : Tiles.Model<VisualizeURLs>() {
                override val initUrls = urls
                override val parentDir = viewModel.roiDir
                override val bounds = viewModel.roi.polygon.coordinates[0].map { LatLng(it[1], it[0]) }

                override fun url(i: Int, urls: VisualizeURLs) = urls.ordered(i)
                override fun tileDir(i: Int) = viewModel.tileDirs[i]
                override fun staleCheck(urls: VisualizeURLs) = StaleCheck(urls.createdAt, urls.timeout)
                override fun getRemote(callback: (ApiResult<VisualizeURLs>) -> Unit) = viewModel.getVisualizeURLs(callback)
                override fun save(urls: VisualizeURLs) = viewModel.saveVisualizeURLsFile(urls)
            })
        }
    }
}