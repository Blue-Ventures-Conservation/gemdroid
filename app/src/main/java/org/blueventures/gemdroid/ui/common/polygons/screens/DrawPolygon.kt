package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object DrawPolygon {
    interface Model {
        var visualizer: Visualize.Visualizer?

        val polygonType: Int
        val drawer: PolygonDrawer

        fun polygonDrawn()
        fun bounds(): List<LatLng>?
    }
    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            val title = stringResource(R.string.draw_polygon).format(stringResource(model.polygonType))
            Visualize.Screen(model.visualizer, title, appBar, draw = Draw.Model(model.drawer, snack) {
                model.polygonDrawn()
                next()
            }, poly = object : Poly.Model() {
                override val menuTitle = ""
                override val touchEnabled = false
                override val labels = emptyList<String>()
                override val bounds: List<LatLng>? = model.bounds()
                override fun polygons(callback: (List<List<List<LatLng>>>) -> Unit) = callback(emptyList())
                override fun <T> markerWork(work: () -> T, callback: (T) -> Unit) = Job()
            })
        }
    }
}