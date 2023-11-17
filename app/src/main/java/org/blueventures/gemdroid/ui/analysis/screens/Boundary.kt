package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.MarkerOptions
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object Boundary {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, back: Click, excluded: Click) {
        Nav.Wrap(back) {
            val coarseBoundary = stringResource(R.string.coarse_boundary)
            Visualize.Screen(viewModel, viewModel.roi.appBarTitle(coarseBoundary), appBar,
                center = Bounds.centerFromList(viewModel.roi.polygonToState()), storage = Maps.Storage.fromViewModel(viewModel),
                poly = object : Poly.Model() {
                    override val touchEnabled = true
                    override fun polygonGroups(callback: (List<Poly.PolygonGroup>) -> Unit) = viewModel.backgroundPolygon { callback(listOf(it)) }
                    override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = viewModel.background(work, callback)
                }
            ) {
                if (viewModel.roi.excludedRegions?.isNotEmpty() == true) {
                    MapActionButton(excluded) { Icon(Icons.Filled.ArrowForward, stringResource(R.string.view_excluded_regions)) }
                }
            }
        }
    }
}