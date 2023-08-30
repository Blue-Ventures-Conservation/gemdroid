package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.blueventures.gemdroid.data.DrawPolygon
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.blend

object Poly {
    abstract class Model {
        private val drawnPolygon = mutableListOf<Polygon>()
        abstract fun polygons(): List<List<List<LatLng>>>

        fun polygonOptions(): List<PolygonOptions> {
            val polys = polygons()
            val size = polys.size
            val opts = mutableListOf<PolygonOptions>()
            polys.forEachIndexed { index, poly ->
                DrawPolygon.opts(poly, fill = blend(MildRed, SkyBlue, index, size, 0x7F))?.let { opts.add(it) }
            }
            return opts
        }

        fun addPolygons(map: GoogleMap) {
            for (opts in polygonOptions()) {
                drawnPolygon.add(map.addPolygon(opts))
            }
        }

        fun clearMapObjects() {
            while(drawnPolygon.isNotEmpty()) {
                drawnPolygon.removeFirst().remove()
            }
        }
    }

    class MapCallback(private val model: Model): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) = model.addPolygons(map)
    }
}