package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object ExcludedRegions {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, back: Click) {
        Nav.Wrap(back) {
            val excludedRegions = viewModel.roi.excludedRegions ?: emptyList()
            val excludedLabel = stringResource(R.string.excluded_regions)
            Visualize.Screen(viewModel, viewModel.roi.appBarTitle(excludedLabel), appBar, center = Bounds.centerFromList(viewModel.roi.polygonToState()), storage = Maps.Storage.fromViewModel(viewModel),
                poly = object : Poly.Model() {
                    override val menuTitle = excludedLabel
                    override val touchEnabled = true
                    override val labels = excludedRegions.map { "" }
                    override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) = viewModel.displayRegions(excludedRegions, callback)
                    override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = viewModel.background(work, callback)
                }
            )
        }
    }
}