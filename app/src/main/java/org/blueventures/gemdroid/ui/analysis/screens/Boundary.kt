package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.MarkerOptions
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Boundary {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar) {
        val coarseBoundary = stringResource(R.string.coarse_boundary)
        Visualize.Screen(viewModel, appBar, viewModel.roi.appBarTitle(coarseBoundary),
            center = Bounds.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), storage = Maps.Storage.fromViewModel(viewModel),
            poly = object : Poly.Model() {
                override val touchEnabled = true
                override fun polygonGroups(callback: (List<Poly.PolygonGroup>) -> Unit) = viewModel.excludedRegions { excludes ->
                    viewModel.backgroundPolygon { coarseROI -> callback(mutableListOf(coarseROI).apply { addAll(excludes) }) }
                }
                override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = viewModel.background(work, callback)
            }
        )
    }
}