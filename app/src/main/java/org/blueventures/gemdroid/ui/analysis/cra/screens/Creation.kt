package org.blueventures.gemdroid.ui.analysis.cra.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.MarkerOptions
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Creation {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBar, done: Click) {
        Visualize.Screen(viewModel.visualizer, appBar, stringResource(R.string.classification_reference_areas),
            true, Bounds.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), Maps.Storage.fromViewModel(viewModel),
            poly = object : Poly.Model() {
                override val touchEnabled = true
                override fun polygonGroups(context: Context, callback: (List<Poly.PolygonGroup>) -> Unit) = viewModel.grouper.polygonGroups(context, false, callback)
                override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = viewModel.background(work, callback)
            })
    }
}