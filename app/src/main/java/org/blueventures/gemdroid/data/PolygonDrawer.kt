package org.blueventures.gemdroid.data

import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import com.squareup.moshi.Json
import org.blueventures.gemdroid.ui.common.maps.Draw
import org.blueventures.gemdroid.ui.theme.Chartreuse
import kotlin.math.sqrt

/**
 * Only draws a single closed polygon with no holes.
 */
class PolygonDrawer(override var points: List<LatLng> = emptyList(), override val maxPoints: Int = 255, private val maxArea: Int? = null): Draw.Data {
    override fun clear() {
        points = emptyList()
    }
    override fun polygonOptions() = ringOpts(points)
    // should be run on a coroutine
    override fun validatePolygon() = if (maxArea != null) validate(maxArea) else area() > 0
    override fun maxHectares() = hectares(maxArea ?: 0)
    override fun polygonHectares() = areaStr(listOf(listOf(points)))
    override fun area() = areaHectares(points)

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

        fun opts(multi: MultiPolyPts, fill: Int = 0x7F00FF00, strokeColor: Int = 0x7F000000, strokeWidth: Float = 4f, dashes: Boolean = false): List<PolygonOptions> {
            val opts = mutableListOf<PolygonOptions>()
            for (poly in multi) {
                opts.add(opt(poly, fill, strokeColor, strokeWidth, dashes))
            }
            return opts
        }

        fun opt(poly: PolyPts, fill: Int = 0x7F00FF00, strokeColor: Int = 0x7F000000, strokeWidth: Float = 4f, dashes: Boolean = false): PolygonOptions {
            val gap = Gap(20f)
            val dash = Dash(20f)

            val opt = PolygonOptions().strokeWidth(strokeWidth).strokeColor(strokeColor).fillColor(fill).strokePattern(if (dashes) listOf(gap, dash) else null).zIndex(Float.MAX_VALUE)

            var first = true
            for (ring in poly) {
                if (first) {
                    opt.addAll(ring)
                    first = false
                    continue
                }

                opt.addHole(ring)
            }

            return opt
        }

        private fun ringOpts(points: List<LatLng>): PolygonOptions? {
            if (points.size < 3) {
                return null
            }

            return opt(listOf(points))
        }
    }

    data class NamedPolygon(
        @Json(name = "name") val name: String,
        @Json(name = "geometry") val polygon: GeojsonMultiPolygon,
    )
}