package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLngBounds
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.maps.Layers
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Polygons

object DynamicsMap {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, details: Click) {
        GetRemote.Save(viewModel::loadDynamicsFile, viewModel::getDynamics, viewModel::saveDynamicsFile, errorHandler = Dynamics::errHandler) { urls ->
            viewModel.urls = urls
            val (boundsResult, setBoundsResult) = remember { mutableStateOf<Result<LatLngBounds?>?>(null) }
            when {
                boundsResult == null -> viewModel.boundsGetter.getInitialLatLngBounds(setBoundsResult)
                else -> {
                    Maps.Screen(appBar, viewModel.roi.appBarTitle(stringResource(R.string.dynamics)), storage = Maps.Storage.fromViewModel(viewModel), bounds = boundsResult.getOrNull(), layers = object : Layers.Model<DynamicsURLs>() {
                        override val initUrls = urls
                        override val layerNames = stringArrayResource(R.array.dynamics_layers).toList()
                        override val parentDir = viewModel.classDir()

                        override fun tileDir(i: Int) = viewModel.tileDirs()[i]
                        override fun getRemote(callback: (Result<DynamicsURLs>) -> Unit) = viewModel.getDynamics(callback)
                        override fun save(urls: DynamicsURLs, callback: (Result<Unit>) -> Unit) = viewModel.saveDynamicsFile(urls, callback)
                    }, poly = object : Polygons.Model() {
                        override val touchEnabled = true
                        override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) { viewModel.polygonGroups(context, callback) }
                        override fun onTouch(point: Polygons.PolygonPoint) = @Composable { Polygons.placeMarker(point) }
                    }, next = Maps.FloatingNext(Icons.Filled.TableChart, R.string.view_dynamics_details, details))
                }
            }
        }
    }
}