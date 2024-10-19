package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.squareup.moshi.Json

typealias PolyPts = List<List<LatLng>>
data class GeojsonPolygon(
    @Json(name = "coordinates") val coordinates: List<List<List<Double>>>,
    @Json(name = "type") val type: String = "Polygon",
) {
    companion object {
        fun toStateWithContainer(geo: GeojsonPolygon, container: MultiPolyPts? = null): Pair<PolyPts, Boolean> {
            val checkContainment = !container.isNullOrEmpty()
            var contained = checkContainment
            val newPoly = mutableListOf<List<LatLng>>()
            for (ring in geo.coordinates) {
                val newRing = mutableListOf<LatLng>()
                for (pt in ring) {
                    val newPt = LatLng(pt[1], pt[0])
                    newRing.add(newPt)

                    if (checkContainment) {
                        for (poly in container!!) {
                            if (poly.isNotEmpty() && !PolyUtil.containsLocation(newPt, poly[0], true)) {
                                contained = false
                            }
                        }
                    }
                }
                newPoly.add(newRing)
            }

            return Pair(newPoly, contained)
        }

        fun fromState(poly: PolyPts): GeojsonPolygon {
            val newPoly = mutableListOf<List<List<Double>>>()
            for (ring in poly) {
                newPoly.add(ringFromState(ring))
            }
            return GeojsonPolygon(newPoly)
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
