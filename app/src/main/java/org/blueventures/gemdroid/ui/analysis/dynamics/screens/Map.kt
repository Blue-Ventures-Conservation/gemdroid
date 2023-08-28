package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Tiles

object Map {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, details: Click, back: Click) {
        Tiles.LayerSetup(R.string.loss, R.string.persistence, R.string.gain)

        GetRemote.Save(viewModel::loadDynamicsFile, viewModel::getDynamics, viewModel::saveDynamicsFile, Dynamics::errHandler) { urls ->
            viewModel.urls = urls

            Maps.Screen(floatingContent = {
                MapActionButton(details) { Icon(Icons.Filled.Info, stringResource(R.string.view_dynamics_details)) }
            }, tiles = object : Tiles.Model<DynamicsURLs>() {
                override val title = viewModel.roi.name + " " + stringResource(R.string.dynamics)
                override val appBar = appBar
                override val initUrls = urls
                override val parentDir = viewModel.classDir()
                override val bounds = viewModel.roi.bounds()
                override fun url(i: Int, urls: DynamicsURLs) = urls.ordered(i)
                override fun tileDir(i: Int) = viewModel.tileDirs()[i]
                override fun getRemote(callback: (ApiResult<DynamicsURLs>) -> Unit) = viewModel.getDynamics(callback)
                override fun save(urls: DynamicsURLs) = viewModel.saveDynamicsFile(urls)
            })
        }
        BackHandler(onBack = back)
    }
}