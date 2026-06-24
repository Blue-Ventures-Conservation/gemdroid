package org.blueventures.gemdroid.ui.common.polygons.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.maps.Visualize

object DrawPolygon {
    interface Model {
        var visualizer: Visualize.Visualizer?

        val storage: Maps.Storage?
        val polygonType: Int
        val drawer: PolygonDrawer
        val attemptGps: Boolean

        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
        fun polygonDrawn()
        fun center(): LatLng?
        fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, next: Click) {
        val title = stringResource(R.string.draw_polygon).format(stringResource(model.polygonType))
        Visualize.Screen(model.visualizer, appBar, title, model.attemptGps, center = model.center(), storage = model.storage, draw = Draw.Model(model.drawer, snack) {
            model.polygonDrawn()
            next()
        }, poly = object : Polygons.Model() {
            override val touchEnabled = false
            override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) = model.polygonGroups(context, callback)
            override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit): Job {
                return model.background({ null }, callback)
            }
        })
    }
}