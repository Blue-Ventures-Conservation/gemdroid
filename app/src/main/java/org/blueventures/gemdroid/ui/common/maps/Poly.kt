package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.DrawPolygon
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.blend

object Poly {
    class Layer(title: String): Tiles.Layer(title) {
        var polygons: MutableList<Polygon>? = null
        var marker: Marker? = null
        override fun toggle(checked: Boolean) {
            clearMarker()
            polygons?.forEach { poly ->
                poly.isVisible = checked
            }
        }

        fun clearMarker() = marker?.remove()

        fun clearPolygons() {
            while(polygons?.isNotEmpty() == true) {
                polygons?.removeFirst()?.remove()
            }
            polygons = mutableListOf()
        }
    }

    abstract class Model {
        private var local: Layer? = null
        abstract val menuTitle: String
        abstract val labels: List<String>
        abstract fun polygons(callback: (List<List<List<LatLng>>>) -> Unit)
        abstract fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit): Job

        fun polygonOptions(callback: (List<PolygonOptions>) -> Unit) {
            polygons { polys ->
                val size = polys.size
                val opts = mutableListOf<PolygonOptions>()
                polys.forEachIndexed { index, poly ->
                    DrawPolygon.opts(poly, fill = blend(MildRed, SkyBlue, index, size, 0x7F))?.let { opts.add(it) }
                }

                callback(opts)
            }
        }

        fun addPolygons(map: GoogleMap, external: Layer?) {
            polygonOptions { optsList ->
                if (local == null) {
                    local = Layer(menuTitle)
                }

                local?.clearPolygons()
                for (opts in optsList) {
                    local?.polygons?.add(map.addPolygon(opts))
                }

                external?.clearPolygons()
                external?.polygons = local?.polygons

                map.setOnMapClickListener { pt ->
                    // Polygon.getPoints must be called on UI thread before we do work
                    val drawnPoints = mutableListOf<List<LatLng>>()
                    local?.polygons?.forEach { poly ->
                        if (poly.isVisible) {
                            drawnPoints.add(poly.points)
                        }
                    }

                    markerWork({
                        var opt: MarkerOptions? = null

                        for (i in 0 until drawnPoints.size) {
                            val points = drawnPoints[i]
                            val label = labels[i]
                            if (PolyUtil.containsLocation(pt, points, true)) {
                                opt = MarkerOptions().position(pt).title(label)
                                break
                            }
                        }

                        opt
                    }) { opt ->
                        local?.clearMarker()
                        opt?.let { local?.marker = map.addMarker(it); local?.marker?.showInfoWindow() }
                        external?.clearMarker()
                        external?.marker = local?.marker
                    }
                }
            }
        }
    }

    class MapCallback(private val model: Model, private val layer: Layer?): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) = model.addPolygons(map, layer)
    }
}