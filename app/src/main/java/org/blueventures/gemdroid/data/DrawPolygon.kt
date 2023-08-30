package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.ui.common.maps.Draw
import java.util.Collections

class DrawPolygon(private val maxArea: Int?, private val adder: (LatLng, (LatLng) -> Unit, (Unit) -> Unit) -> Job): Draw.Data {
    var points = mutableListOf<LatLng>()

    override val markers = mutableListOf<Marker>()
    override var polygon: Polygon? = null

    override fun polygonOptions() = ringOpts(points)
    // should be run on a coroutine
    override fun addPoint(point: LatLng, callback: (Unit) -> Unit): Job  = adder(point, ::addPoint, callback)
    override fun polygonIterate(mapf: (LatLng) -> Unit) = points.iterator().forEach(mapf)
    override fun points() = points
    override fun clearPoints() { points = mutableListOf() }
    override fun validatePolygon() = if (maxArea != null) validate(maxArea) else area() > 0
    override fun maxSquareKms() = squareKms(maxArea ?: 0)
    override fun polygonSquareKms() = areaStr()
    override fun area() = SphericalUtil.computeArea(points)/squareKmInMeters

    private fun addPoint(point: LatLng) {
        if (points.size > 2) {
            val distances = mutableListOf<Double>()

            for (i in points.indices) {
                // 1. Find the start and end points of the next line segment
                var start: LatLng
                var end: LatLng
                if (i == points.size - 1) {
                    start = points.last()
                    end = points.first()
                } else {
                    start = points[i]
                    end = points[i+1]
                }

                // 2. Calculate the nearest coordinate by finding distance between the line segment and the coordinate to be drawn
                val distance = PolyUtil.distanceToLine(point, start, end)
                distances.add(distance)
            }

            // 3. The nearest coordinate = the edge with minimum distance to the coordinate to be drawn
            // in some cases, there may be two edges the same distance from the new point, then
            // we check to see which potential polygon has a positive signed area to avoid self intersections
            val min = Collections.min(distances)
            val last = distances.lastIndexOf(min)
            val first = distances.indexOf(min)
            val position =  when {
                last == first -> first
                signedArea(points, first, point) > signedArea(points, last, point) -> first
                else -> last
            }

            // 4. move the nearest coordinate at the end by shifting array right
            points = rotate(points, position)
        }

        // 5. Now add coordinate to be drawn
        points.add(point)
    }

    fun areaStr() = squareKms(area().toInt())

    fun validate(max: Int): Boolean {
        val area = area()
        return area > 0 && area <= max
    }

    companion object {
        const val squareKmInMeters = 1_000_000

        fun squareKms(km: Int) = "${"%,d".format(km)} km²"

        fun ringOpts(points: List<LatLng>, stroke: Float = 2f, fill: Int = 0x7F00FF00): PolygonOptions? {
            if (points.size < 3) {
                return null
            }

            return opts(listOf(points), stroke, fill)
        }

        fun opts(points: List<List<LatLng>>, stroke: Float = 2f, fill: Int = 0x7F00FF00): PolygonOptions? {
            val opts = PolygonOptions().strokeWidth(stroke).fillColor(fill).zIndex(Float.MAX_VALUE)
            for (ring in points) {
                opts.addAll(ring)
            }
            return opts
        }

        private fun signedArea(points: List<LatLng>, nearestIdx: Int, point: LatLng): Double {
            // toMutableList makes a copy so we aren't modifying the original list
            val poly = rotate(points.toMutableList(), nearestIdx)
            poly.add(point)
            return SphericalUtil.computeSignedArea(poly)
        }

        private fun <T> rotate(points: MutableList<T>, nearestIdx: Int): MutableList<T> {
            val shift = points.size - (nearestIdx + 1)

            if (shift <= 0 || shift == points.size) {
                return points
            }

            var element: T?
            for (i in 0 until shift) {
                // remove last element, add it to front of the List
                element = points.removeAt(points.size - 1)
                points.add(0, element)
            }
            return points
        }
    }
}