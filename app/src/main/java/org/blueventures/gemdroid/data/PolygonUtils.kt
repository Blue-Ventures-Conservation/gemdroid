package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import com.squareup.moshi.Json

object PolygonUtils {
    const val HECTARE_IN_METERS = 10_000
    const val MAX_AOI_HECTARES = 2_500_000

    fun center(points: List<LatLng>, default: LatLng?): LatLng? {
        return if (points.isEmpty()) {
            default
        } else {
            centerFromRing(points)
        }
    }

    fun validate(points: List<LatLng>) = validateMax(points, MAX_AOI_HECTARES)
    fun validateMax(points: List<LatLng>, max: Int): Boolean {
        val area = ringAreaHectares(points)
        return area > 0 && area <= max
    }

    fun areaStr(polygon: PolyPts) = hectaresString(polygonAreaHectares(polygon).toInt())

    fun multiPolygonAreaHectares(multi: MultiPolyPts): Double {
        var total = 0.0
        for (poly in multi) {
            total += polygonAreaHectares(poly)
        }
        return total
    }

    fun polygonAreaHectares(polygon: PolyPts): Double {
        var total = 0.0
        var first = true
        for (ring in polygon) {
            if (first) {
                total += ringAreaHectares(ring)
                first = false
                continue
            }

            // TODO: this should be just a subtraction of the intersection between the outer ring and this inner ring
            // we will likely need to import the JST and do this work on a background thread to get a better calculation

            total -= ringAreaHectares(ring)
        }

        return total
    }
    fun ringAreaHectares(points: List<LatLng>) = SphericalUtil.computeArea(points)/HECTARE_IN_METERS

    fun hectaresString(ha: Int) = "${"%,d".format(ha)} ha"
    fun maxHectaresString() = hectaresString(MAX_AOI_HECTARES)

    fun opts(multi: MultiPolyPts, fill: Int = 0x7F00FF00, strokeColor: Int = 0x7F000000, strokeWidth: Float = 4f, dashes: Boolean = false): List<PolygonOptions>? {
        val opts = mutableListOf<PolygonOptions>()
        for (poly in multi) {
            val opt = opt(poly, fill, strokeColor, strokeWidth, dashes) ?: return null
            opts.add(opt)
        }
        return opts
    }

    fun opt(poly: PolyPts, fill: Int = 0x7F00FF00, strokeColor: Int = 0x7F000000, strokeWidth: Float = 4f, dashes: Boolean = false): PolygonOptions? {
        val opt = ringOpt(poly.first(), fill, strokeColor, strokeWidth, dashes) ?: return null
        var first = true
        for (ring in poly) {
            if (first) {
                first = false
                continue
            }

            opt.addHole(ring)
        }

        return opt
    }

    fun ringOpt(ring: List<LatLng>, fill: Int = 0x7F00FF00, strokeColor: Int = 0x7F000000, strokeWidth: Float = 4f, dashes: Boolean = false): PolygonOptions? {
        if (ring.size < 3) return null

        val gap = Gap(20f)
        val dash = Dash(20f)
        return PolygonOptions()
            .strokeWidth(strokeWidth)
            .strokeColor(strokeColor)
            .fillColor(fill)
            .strokePattern(if (dashes) listOf(gap, dash) else null)
            .zIndex(Float.MAX_VALUE)
            .addAll(ring)
    }

    fun centerFromMultiPoly(multi: MultiPolyPts): LatLng? {
        if (multi.isEmpty()) {
            return null
        }

        val builder = LatLngBounds.builder()
        for (poly in multi) {
            for (ring in poly) {
                for (pt in ring) {
                    builder.include(pt)
                }
            }
        }

        return builder.build().center
    }

    fun centerFromRing(ring: List<LatLng>): LatLng? {
        if (ring.isEmpty()) {
            return null
        }

        val builder = LatLngBounds.builder()
        for (pt in ring) {
            builder.include(pt)
        }

        return builder.build().center
    }

    data class NamedPolygon(
        @Json(name = "name") val name: String,
        @Json(name = "geometry") val polygon: GeojsonMultiPolygon,
    )
}