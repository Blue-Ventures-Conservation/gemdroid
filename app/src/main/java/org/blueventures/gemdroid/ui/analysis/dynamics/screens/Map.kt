package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Tiles

object Map {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, details: Click) {
        Nav.Wrap(back) {
            Dynamics(viewModel, appBar, details)
        }
    }

    @Composable
    fun Dynamics(viewModel: DynamicsViewModel, appBar: AppBar, details: Click) {
        GetRemote.Save(viewModel::loadDynamicsFile, viewModel::getDynamics, viewModel::saveDynamicsFile, errorHandler = Dynamics::errHandler) { urls ->
            viewModel.urls = urls

            Maps.Screen(floating = {
                MapActionButton(details) { Icon(Icons.Filled.Info, stringResource(R.string.view_dynamics_details)) }
            }, tiles = object : Tiles.Model<DynamicsURLs>() {
                override val title = viewModel.roi.appBarTitle(stringResource(R.string.dynamics))
                override val appBar = appBar
                override val initUrls = urls
                override val layerNames = stringArrayResource(R.array.dynamics_layers).toList()
                override val parentDir = viewModel.classDir()
                override val bounds = viewModel.roi.bounds()

                override fun tileDir(i: Int) = viewModel.tileDirs()[i]
                override fun getRemote(callback: (ApiResult<DynamicsURLs>) -> Unit) = viewModel.getDynamics(callback)
                override fun save(urls: DynamicsURLs) = viewModel.saveDynamicsFile(urls)
            }, poly = object : Poly.Model() {
                override val menuTitle = stringResource(R.string.sub_regions)
                override val labels = viewModel.subRegions.map { it.name }
                override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) = viewModel.displayRegions(callback)
                override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = viewModel.background(work, callback)
            })
        }
    }
}