package org.blueventures.gemdroid.ui.common.polygons.screens

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.maps.Visualize

object DrawPolygon {
    interface Model : Draw.Data {
        fun visualizer(): Visualize.Visualizer?
        fun center(): LatLng?
        fun polygonDrawn()
        fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit)
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, storage: Maps.Storage?, @StringRes polygonType: Int, attemptGps: Boolean, next: Click) {
        val title = stringResource(R.string.draw_polygon).format(stringResource(polygonType))
        Visualize.Screen(model.visualizer(), appBar, title, attemptGps, center = model.center(), storage = storage, draw = Draw.Model(model, snack) {
            model.polygonDrawn()
            next()
        }, poly = object : Polygons.Model() {
            override val touchEnabled = false
            override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) { model.polygonGroups(context, callback) }
            override fun onTouch(point: Polygons.PolygonPoint?) = @Composable { Polygons.PlaceMarker(point) }
        })
    }
}