package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json

typealias MultiPolyPts = List<List<List<LatLng>>>
data class GeojsonMultiPolygon(
    @Json(name = "coordinates") val coordinates: List<List<List<List<Double>>>>,
    @Json(name = "type") val type: String = "MultiPolygon",
) {
    companion object {
        fun toState(geo: GeojsonMultiPolygon): MultiPolyPts {
            val multi = mutableListOf<List<List<LatLng>>>()
            for (poly in geo.coordinates) {
                val newPoly = GeojsonPolygon.toState(GeojsonPolygon(poly))
                multi.add(newPoly)
            }

            return multi
        }

        fun fromState(polys: MultiPolyPts): GeojsonMultiPolygon {
            val multi = mutableListOf<List<List<List<Double>>>>()
            for (poly in polys) {
                val newPoly = GeojsonPolygon.fromState(poly)
                multi.add(newPoly.coordinates)
            }
            return GeojsonMultiPolygon(multi)
        }
    }
}