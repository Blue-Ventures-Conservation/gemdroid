package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Maps

object Visualize {
    @StringRes
    private const val title = R.string.visualize_imagery_title

    private lateinit var chot: Maps.Layer
    private lateinit var clot: Maps.Layer
    private lateinit var hhot: Maps.Layer
    private lateinit var hlot: Maps.Layer

    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, back: Click) {
        appBar(AppBarUpdate(stringResource(title)))

        Maps.LayerSetup({ layers ->
            chot = layers[0]
            clot = layers[1]
            hhot = layers[2]
            hlot = layers[3]
        }, R.string.cont_high_tide, R.string.cont_low_tide, R.string.hist_high_tide, R.string.hist_low_tide)

        GetRemote.Save(viewModel::loadVisualizeURLsFile, viewModel::getVisualizeURLs, viewModel::saveVisualizeURLsFile) { urls ->
            VisualizeMap(viewModel, urls, appBar)
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun VisualizeMap(viewModel: AnalysisViewModel, visualizeURLs: VisualizeURLs, appBar: AppBarFun) {
        Maps.Screen(stringResource(title), visualizeURLs, viewModel.roiDir, appBar, staleCheck = { urls ->
            staleCheck(urls.createdAt, urls.timeout)
        }, tileDir = { i ->
            viewModel.tileDirs[i]
        }, url = { i, urls ->
            urls.ordered(i)
        }, viewModel::getVisualizeURLs, viewModel::saveVisualizeURLsFile, {}, viewModel.roi.polygon.coordinates[0], chot, clot, hhot, hlot)
    }
}