package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Maps

object Visualize {
    @StringRes
    private const val title = R.string.visualize_imagery_title

    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBarFun, back: Click) {
        appBar(AppBarUpdate(stringResource(title)))
        GetRemote.Save(viewModel::loadVisualizeURLsFile, viewModel::getVisualizeURLs, viewModel::saveVisualizeURLsFile) { urls ->
            VisualizeMap(viewModel, urls, appBar)
        }

        BackHandler(onBack = back)
    }

    private val chot = Maps.Layer("Contemporary High Tide")
    private val clot = Maps.Layer("Contemporary Low Tide")
    private val hhot = Maps.Layer("Historical High Tide")
    private val hlot = Maps.Layer("Historical Low Tide")

    @Composable
    fun VisualizeMap(viewModel: AnalysisViewModel, visualizeURLs: VisualizeURLs, appBar: AppBarFun) {
        Maps.Screen(stringResource(title), visualizeURLs, viewModel.roiDir, appBar, staleCheck = { urls ->
            (System.currentTimeMillis() / 1000) - urls.createdAt > urls.timeout
        }, tileDir = { i ->
            viewModel.tileDirs[i]
        }, url = { i, urls ->
            urls.ordered(i)
        }, viewModel::getVisualizeURLs, viewModel::saveVisualizeURLsFile, viewModel.roi.polygon.coordinates[0], chot, clot, hhot, hlot)
    }
}