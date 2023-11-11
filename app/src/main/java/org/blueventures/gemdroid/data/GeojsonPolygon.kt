package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json
typealias PolyPts = List<List<LatLng>>
typealias FromStateWork = (() -> GeojsonPolygon, (GeojsonPolygon) -> Unit) -> Unit
typealias ToStateWork = (() -> PolyPts, (PolyPts) -> Unit) -> Unit
data class GeojsonPolygon(
    @Json(name = "coordinates") val coordinates: List<List<List<Double>>>,
    @Json(name = "type") val type: String = "Polygon",
) {
    companion object {
        fun toState(bg: ToStateWork, geo: GeojsonPolygon, callback: (PolyPts) -> Unit) {
            bg({ toState(geo) }, callback)
        }

        fun toState(geo: GeojsonPolygon): PolyPts {
            val poly = mutableListOf<List<LatLng>>()
            for (ring in geo.coordinates) {
                val newRing = mutableListOf<LatLng>()
                for (pt in ring) {
                    newRing.add(LatLng(pt[1], pt[0]))
                }
                poly.add(newRing)
            }

            return poly
        }

        fun fromState(bg: FromStateWork, points: PolyPts, callback: (GeojsonPolygon) -> Unit) {
            bg({ fromState(points) }, callback)
        }

        fun fromState(points: PolyPts): GeojsonPolygon {
            val poly = mutableListOf<List<List<Double>>>()
            for (ring in points) {
                val convert = ringFromState(ring)
                poly.add(convert)
            }
            return GeojsonPolygon(poly)
        }

        fun ringFromState(points: List<LatLng>): List<List<Double>> {
            val ring = mutableListOf<List<Double>>()
            for (point in points) {
                ring.add(listOf(point.longitude, point.latitude))
            }

            val first = points.first()
            if (first != points.last()) {
                ring.add(listOf(first.longitude, first.latitude))
            }

            return ring
        }
    }
}