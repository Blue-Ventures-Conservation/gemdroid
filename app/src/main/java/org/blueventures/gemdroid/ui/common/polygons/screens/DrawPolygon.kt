package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Visualize

object DrawPolygon {
    interface Model {
        var visualizer: Visualize.Visualizer?

        val storage: Maps.Storage?
        val polygonType: Int
        val drawer: PolygonDrawer

        fun polygonDrawn()
        fun center(): LatLng?
    }
    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            val title = stringResource(R.string.draw_polygon).format(stringResource(model.polygonType))
            Visualize.Screen(model.visualizer, title, appBar, center = model.center(), storage = model.storage, draw = Draw.Model(model.drawer, snack) {
                model.polygonDrawn()
                next()
            })
        }
    }
}