package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.squareup.moshi.Json
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.ui.common.maps.Draw
import java.util.Collections

class PolygonDrawer(private val points: MutableList<LatLng> = mutableListOf(), private val maxArea: Int? = null, private val background: (() -> Boolean, (Boolean) -> Unit) -> Job): Draw.Data {
    override fun points() = points
    override fun clear() { points.clear(); ordered.clear() }
    override fun polygonOptions() = ringOpts(points)
    // should be run on a coroutine
    override fun addPoint(point: LatLng, callback: () -> Unit): Job  = background({ addPoint(point); true }, { callback() })
    override fun removePrev(callback: (Boolean) -> Unit) = background(::removePrevious) { callback(it) }
    override fun validatePolygon() = if (maxArea != null) validate(maxArea) else area() > 0
    override fun maxSquareKms() = squareKms(maxArea ?: 0)
    override fun polygonSquareKms() = areaStr(points)
    override fun area() = areaKms(points)

    private val ordered = mutableListOf<LatLng>()

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
                areaMeters(points, first, point) > areaMeters(points, last, point) -> first
                else -> last
            }

            // 4. move the nearest coordinate at the end by shifting array right
            rotate(points, position)
        }

        // 5. Now add coordinate to be drawn
        ordered.add(point)
        points.add(point)
    }

    private fun removePrevious(): Boolean {
        if (ordered.isEmpty() || points.isEmpty()) {
            return false
        }

        points.removeLast()
        ordered.removeLast()

        if (ordered.isEmpty() || points.isEmpty()) {
            return true
        }

        val next = ordered.last()
        val index = points.lastIndexOf(next)
        if (index != points.size - 1) {
            rotate(points, index)
        }

        return true
    }

    private fun validate(max: Int): Boolean {
        val area = areaKms(points)
        return area > 0 && area <= max
    }

    companion object {
        const val squareKmInMeters = 1_000_000

        fun areaStr(points: List<LatLng>) = squareKms(areaKms(points).toInt())
        fun squareKms(km: Int) = "${"%,d".format(km)} km²"
        fun areaKms(points: List<LatLng>) = SphericalUtil.computeArea(points)/squareKmInMeters

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

        private fun areaMeters(points: List<LatLng>, nearestIdx: Int, point: LatLng): Double {
            // toMutableList makes a copy so we aren't modifying the original list
            val mut = points.toMutableList()
            rotate(mut, nearestIdx)
            mut.add(point)
            return SphericalUtil.computeArea(mut)
        }

        private fun <T> rotate(points: MutableList<T>, nearestIdx: Int) {
            val shift = points.size - (nearestIdx + 1)

            if (shift <= 0 || shift == points.size) {
                return
            }

            var element: T?
            for (i in 0 until shift) {
                // remove last element, add it to front of the List
                element = points.removeAt(points.size - 1)
                points.add(0, element)
            }
        }
    }

    data class NamedPolygon(
        @Json(name = "name") val name: String,
        @Json(name = "geometry") val polygon: GeojsonPolygon,
    )
}