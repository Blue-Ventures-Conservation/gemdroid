package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.squareup.moshi.Json
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.maps.Draw
import java.util.Collections

/**
 * Only draws a single closed polygon with no holes.
 */
class PolygonDrawer(override val maxPoints: Int = 100, private val points: MutableList<LatLng> = mutableListOf(), private val maxArea: Int? = null, private val background: (() -> Int?, (Int?) -> Unit) -> Job): Draw.Data {
    override fun points() = points
    override fun clear() { points.clear(); ordered.clear() }
    override fun polygonOptions() = ringOpts(points)
    // should be run on a coroutine
    override fun addPoint(point: LatLng, callback: (Int?) -> Unit): Job  = background({ addPoint(point) }, { callback(it) })
    override fun removePrev(callback: (Int?) -> Unit) = background(::removePrevious) { callback(it) }
    override fun validatePolygon() = if (maxArea != null) validate(maxArea) else area() > 0
    override fun maxHectares() = hectares(maxArea ?: 0)
    override fun polygonHectares() = areaStr(listOf(listOf(points)))
    override fun area() = areaHectares(points)

    private val ordered = mutableListOf<LatLng>()

    private fun addPoint(point: LatLng): Int? {
        if (points.size >= maxPoints) {
            return R.string.poly_too_big
        }
        
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
        return null
    }

    private fun removePrevious(): Int? {
        if (ordered.isEmpty() || points.isEmpty()) {
            return null
        }

        points.removeLast()
        ordered.removeLast()

        if (ordered.isEmpty() || points.isEmpty()) {
            return null
        }

        val next = ordered.last()
        val index = points.lastIndexOf(next)
        if (index != points.size - 1) {
            rotate(points, index)
        }

        return 0
    }

    private fun validate(max: Int): Boolean {
        val area = areaHectares(points)
        return area > 0 && area <= max
    }

    companion object {
        const val hectareInMeters = 10_000

        fun areaStr(multi: MultiPolyPts) = hectares(areaHectares(multi))
        fun areaHectares(multi: MultiPolyPts): Int {
            var total = 0.0
            for (poly in multi) {
                var first: List<LatLng>? = null
                for (ring in poly) {
                    if (first == null) {
                        total += areaHectares(ring)
                        first = ring
                        continue
                    }

                    // TODO: this should be just a subtraction of the intersection between the outer ring and this inner ring
                    // we will likely need to import the JST and do this work on a background thread to get a better calculation

                    total -= areaHectares(ring)
                }
            }
            return total.toInt()
        }
        fun hectares(ha: Int) = "${"%,d".format(ha)} ha"
        fun areaHectares(points: List<LatLng>) = SphericalUtil.computeArea(points)/hectareInMeters

        fun opts(multi: MultiPolyPts, fill: Int = 0x7F00FF00, strokeColor: Int = 0x7F000000, dashes: Boolean = false): List<PolygonOptions> {
            val gap = Gap(20f)
            val dash = Dash(20f)

            val opts = mutableListOf<PolygonOptions>()
            for (poly in multi) {
                val opt = PolygonOptions().strokeWidth(4f).strokeColor(strokeColor).fillColor(fill).strokePattern(if (dashes) listOf(gap, dash) else null).zIndex(Float.MAX_VALUE)

                var first = true
                for (ring in poly) {
                    if (first) {
                        opt.addAll(ring)
                        first = false
                        continue
                    }

                    opt.addHole(ring)
                }

                opts.add(opt)
            }
            return opts
        }

        private fun ringOpts(points: List<LatLng>): PolygonOptions? {
            if (points.size < 3) {
                return null
            }

            val options = opts(listOf(listOf(points)))
            if (options.isEmpty()) {
                return null
            }

            return options.first()
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
        @Json(name = "geometry") val polygon: GeojsonMultiPolygon,
    )
}