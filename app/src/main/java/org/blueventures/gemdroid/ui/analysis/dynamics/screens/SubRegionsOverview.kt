package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object SubRegionsOverview {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, ok: Click, startOver: Click) {
        Nav.Wrap(back) {
            Info.Block {
                Info.Row {
                    Butt.Text(stringResource(R.string.start_over), click = startOver)
                    Butt.Text(stringResource(R.string.looks_good), click = ok)
                }

                Visualize.Screen(viewModel.visualizer, stringResource(R.string.review_sub_regions), appBar, poly = object : Poly.Model() {
                    override val menuTitle = stringResource(R.string.sub_regions)
                    override val labels = viewModel.subRegions.map { it.name }
                    override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) = viewModel.displayRegions(callback)
                })
            }
        }
    }
}