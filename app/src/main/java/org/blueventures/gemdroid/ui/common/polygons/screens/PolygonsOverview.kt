package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object PolygonsOverview {
    interface Model {
        var visualizer: Visualize.Visualizer?

        val storage: Maps.Storage?
        val polygonTypePlural: Int

        fun center(): LatLng?
        fun displayRegions(callback: (List<Poly.PolygonGroup>) -> Unit): Job
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, ok: Click) {
        Info.Block {
            val title = stringResource(R.string.review_polygons).format(stringResource(model.polygonTypePlural))

            Visualize.Screen(model.visualizer, title, appBar, center = model.center(), storage = model.storage, poly = object : Poly.Model() {
                override val touchEnabled = true
                override fun polygonGroups(callback: (List<Poly.PolygonGroup>) -> Unit) = model.displayRegions(callback)
                override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = model.background(work, callback)
            }) {
                MapActionButton(ok) {
                    Icon(Icons.Filled.Check, stringResource(R.string.polygons_look_good))
                }
            }
        }
    }
}