package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object PolygonsOverview {
    interface Model {
        var visualizer: Visualize.Visualizer?

        val polygonTypePlural: Int
        val polygons: MutableList<PolygonDrawer.NamedPolygon>

        fun displayRegions(callback: (List<List<List<LatLng>>>) -> Unit)
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, back: Click, ok: Click) {
        Nav.Wrap(back) {
            Info.Block {
                val plural = stringResource(model.polygonTypePlural)
                val title = stringResource(R.string.review_polygons).format(plural)

                Visualize.Screen(model.visualizer, title, appBar, poly = object : Poly.Model() {
                    override val menuTitle = plural
                    override val touchEnabled = true
                    override val labels = model.polygons.map { it.name }
                    override val bounds = if (model.polygons.isNotEmpty()) GeojsonPolygon.toState(model.polygons.first().polygon).first()  else null
                    override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) = model.displayRegions(callback)
                    override fun <T> markerWork(work: () -> T, callback: (T) -> Unit) = model.background(work, callback)
                }) {
                    MapActionButton(ok) {
                        Icon(Icons.Filled.Check, stringResource(R.string.polygons_look_good))
                    }
                }
            }
        }
    }
}