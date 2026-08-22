package org.blueventures.gemdroid.ui.analysis.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Boundary {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar) {
        val coarseBoundary = stringResource(R.string.coarse_boundary)
        Visualize.Screen(viewModel, appBar, viewModel.roi.appBarTitle(coarseBoundary),
            center = PolygonUtils.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), storage = Maps.Storage.fromViewModel(viewModel),
            poly = object : Polygons.Model() {
                override val touchEnabled = true
                override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) { viewModel.polygonGroups(context, true, callback) }
                override fun onTouch(point: Polygons.PolygonPoint?) = @Composable { Polygons.PlaceMarker(point) }
            }
        )
    }
}