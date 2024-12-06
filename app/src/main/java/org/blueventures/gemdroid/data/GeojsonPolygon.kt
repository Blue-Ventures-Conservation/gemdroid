package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.hectareInMeters

typealias PolyPts = List<List<LatLng>>
data class GeojsonPolygon(
    @Json(name = "coordinates") val coordinates: List<List<List<Double>>>,
    @Json(name = "type") val type: String = "Polygon",
) {
    companion object {
        fun toStateWithContainer(geo: GeojsonPolygon, container: MultiPolyPts? = null): Pair<PolyPts, Int> {
            val checkContainment = !container.isNullOrEmpty()
            val newPoly = mutableListOf<List<LatLng>>()

            val uncontainedRing = mutableListOf<LatLng>()
            var first = true
            for (ring in geo.coordinates) {
                val newRing = mutableListOf<LatLng>()
                for (pt in ring) {
                    val newPt = LatLng(pt[1], pt[0])
                    newRing.add(newPt)

                    if (checkContainment && first) {
                        var ptContained = false
                        for (poly in container!!) {
                            if (PolyUtil.containsLocation(newPt, poly[0], true)) {
                                ptContained = true
                                break
                            }
                        }

                        if (!ptContained) {
                            uncontainedRing.add(newPt)
                        }
                    }
                }
                first = false
                newPoly.add(newRing)
            }

            // TODO: this should be just be a polygon difference, rather than building a diff polygon with the points that lie outside
            // we will likely need to import the JST and do this work on a background thread to get a better calculation

            var hectaresOutside = 0.0
            if (uncontainedRing.size >= 3) {
                // close the ring
                uncontainedRing.add(uncontainedRing.first())
                hectaresOutside = SphericalUtil.computeArea(uncontainedRing) / hectareInMeters
            }
            return Pair(newPoly, hectaresOutside.toInt())
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
