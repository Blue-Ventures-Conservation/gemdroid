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
    abstract class Model {
        private val drawnPolygons = mutableListOf<Polygon>()
        private var marker: Marker? = null

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

        fun addPolygons(map: GoogleMap) {
            polygonOptions { optsList ->
                for (opts in optsList) {
                    drawnPolygons.add(map.addPolygon(opts))
                }

                map.setOnMapClickListener { pt ->
                    // Polygon.getPoints must be called on UI thread before we do work
                    val drawnPoints = mutableListOf<List<LatLng>>()
                    for (drawn in drawnPolygons) {
                        drawnPoints.add(drawn.points)
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
                        marker?.remove()
                        opt?.let { marker = map.addMarker(it); marker?.showInfoWindow() }
                    }
                }
            }
        }

        fun clearMapObjects() {
            marker?.remove()
            while(drawnPolygons.isNotEmpty()) {
                drawnPolygons.removeFirst().remove()
            }
        }
    }

    class MapCallback(private val model: Model): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) = model.addPolygons(map)
    }
}