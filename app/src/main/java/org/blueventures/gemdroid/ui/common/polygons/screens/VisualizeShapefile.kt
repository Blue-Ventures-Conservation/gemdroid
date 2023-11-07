package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object VisualizeShapefile {
    interface Model {
        var name: String
        var visualizer: Visualize.Visualizer?
        var shapefile: List<List<LatLng>>

        fun shapefileLooksGood()
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, back: Click, next: Click) {
        Nav.Wrap(back) {
            val title = stringResource(R.string.visualize_shp)
            Visualize.Screen(model.visualizer, title, appBar, poly = object : Poly.Model() {
                override val menuTitle = stringResource(R.string.shapefile)
                override val touchEnabled = true
                override val labels = listOf(model.name)
                override val bounds = if (model.shapefile.isNotEmpty()) model.shapefile.first() else null
                override fun <T> markerWork(work: () -> T, callback: (T) -> Unit) = model.background(work, callback)

                override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) {
                    if (model.shapefile.isEmpty()) {
                        callback(emptyList())
                    } else {
                        callback(listOf(model.shapefile))
                    }
                }
            }, floating = {
                MapActionButton({
                    model.shapefileLooksGood()
                    next()
                }) { Icon(Icons.Filled.Check, stringResource(R.string.shp_looks_good)) }
            })
        }
    }
}