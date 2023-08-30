package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json

data class GeojsonPolygon(
    @Json(name = "coordinates") val coordinates: List<List<List<Double>>>,
    @Json(name = "type") val type: String = "Polygon",
) {
    companion object {
        fun toState(geo: GeojsonPolygon): List<List<LatLng>> {
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

        fun fromState(points: List<List<LatLng>>): GeojsonPolygon {
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