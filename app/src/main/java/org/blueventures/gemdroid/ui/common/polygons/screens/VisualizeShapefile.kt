package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize

object VisualizeShapefile {
    interface Model {
        var polygonName: String
        var visualizer: Visualize.Visualizer?
        var shapefile: MultiPolyPts

        val storage: Maps.Storage?
        val shpColor: Int?

        fun shapefileLooksGood()
        fun backgroundPolygon(callback: (Poly.PolygonGroup?) -> Unit): Job
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, next: Click) {
        val (gotCenter, setGotCenter) = remember { mutableStateOf(false) }
        val (center, setCenter) = remember { mutableStateOf<LatLng?>(null) }
        if (!gotCenter) {
            model.background({
                Bounds.centerFromMultiPoly(model.shapefile)
            }) { cent ->
                setGotCenter(true)
                setCenter(cent)
            }
        } else {
            val title = stringResource(R.string.visualize_shp)
            Visualize.Screen(model.visualizer, appBar, title, false, center = center, storage = model.storage, poly = object : Poly.Model() {
                override val touchEnabled = true
                override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = model.background(work, callback)

                override fun polygonGroups(callback: (List<Poly.PolygonGroup>) -> Unit): Job {
                    return model.backgroundPolygon { bg ->
                        val list = if (model.shapefile.isEmpty()) mutableListOf() else mutableListOf(Poly.PolygonGroup(R.string.shapefile, listOf(Poly.NamedPoly(model.polygonName, model.shapefile)), model.shpColor))
                        if (bg != null) list.add(bg)
                        callback(list)
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