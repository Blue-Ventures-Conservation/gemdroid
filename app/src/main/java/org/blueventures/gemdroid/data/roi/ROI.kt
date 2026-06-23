package org.blueventures.gemdroid.data.roi

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.shouldUseS2
import java.io.File

// Saved on device based on user input when creating an ROI
data class ROI(
    @Json(name = "buff_dist") val buffDist: Int = -1,
    @Json(name = "name") val name: String = "",
    @Json(name = "hist_year_start") val histYearStart: Int = 0,
    @Json(name = "hist_year_end") val histYearEnd: Int = 0,
    @Json(name = "hist_months") val histMonths: List<Int>? = null,
    @Json(name = "cont_year_start") val contYearStart: Int = 0,
    @Json(name = "cont_year_end") val contYearEnd: Int = 0,
    @Json(name = "cont_months") val contMonths: List<Int>? = null,
    @Json(name = "polygon") val polygon: GeojsonMultiPolygon = GeojsonMultiPolygon(emptyList()),
    @Json(name = "inland_mang") val inlandMang: Boolean = false,
    @Json(name = "excludes") val excludedRegions: List<GeojsonMultiPolygon> = emptyList(),
    @Json(name = "visualize") val visualize: Boolean = true,
    @Json(name = "region_uuid") val regionUUID: String? = null,
    @Json(name = "force_landsat") val forceLandsat: Boolean? = null,

    // old and unused, kept for backwards compatibility
    @Json(name = "cont_month_start") val contMonthStart: Int? = null,
    @Json(name = "cont_month_end") val contMonthEnd: Int? = null,
    @Json(name = "hist_month_start") val histMonthStart: Int? = null,
    @Json(name = "hist_month_end") val histMonthEnd: Int? = null,
) {
    fun boundaryPolyToState() = if (polygon.coordinates.isNotEmpty()) GeojsonMultiPolygon.toState(polygon) else emptyList()

    fun appBarTitle(title: String) = "$name $title"

    fun useS2(): Boolean {
        val pair = roiMonths()
        return !(forceLandsat ?: true) && shouldUseS2(histYearStart, pair.first, contYearStart, pair.second)
    }

    fun roiMonths(): Pair<List<Int>, List<Int>> {
        if (histMonths != null && contMonths != null) {
            return Pair(histMonths, contMonths)
        }

        if (histMonthStart != null && histMonthEnd != null && contMonthStart != null && contMonthEnd != null) {
            return Pair(getMonthsFromRange(histMonthStart, histMonthEnd), getMonthsFromRange(contMonthStart, contMonthEnd))
        }

        return Pair(listOf(), listOf())
    }

    companion object : Serializer<ROI>() {
        private val adapter = make<ROI>()

        fun fromState(
            name: String,
            histYearStart: Int,
            histYearEnd: Int,
            histMonths: List<Int>,
            contYearStart: Int,
            contYearEnd: Int,
            contMonths: List<Int>,
            points: MultiPolyPts,
            inlandMang: Boolean,
            excludes: List<PolygonDrawer.NamedPolygon>,
            buffDist: Int,
            regionUUID: String? = null,
            forceLandsat: Boolean? = null,
        ): ROI {
            return ROI(
                buffDist,
                name,
                histYearStart,
                histYearEnd,
                histMonths,
                contYearStart,
                contYearEnd,
                contMonths,
                GeojsonMultiPolygon.fromState(points),
                inlandMang,
                excludes.map { it.polygon },
                regionUUID = regionUUID,
                forceLandsat = forceLandsat,
            )
        }

        override fun fromFile(file: File): Result<ROI> {
            val res = fromFile(adapter, file)
            return when {
                res.isSuccess -> {
                    val mult = res.getOrNull()!!
                    val pair = mult.roiMonths()
                    Result.success(mult.copy(histMonths = pair.first, contMonths = pair.second))
                }
                else -> {
                    val singleRes = SinglePolyROI.fromFile(file)
                    when {
                        singleRes.isSuccess -> {
                            val single = singleRes.getOrNull()!!
                            val multiExcludes = mutableListOf<GeojsonMultiPolygon>()
                            for (poly in single.excludedRegions) {
                                multiExcludes.add(GeojsonMultiPolygon(listOf(poly.coordinates)))
                            }

                            Result.success(ROI(single.buffDist, single.name, single.histYearStart, single.histYearEnd, getMonthsFromRange(single.histMonthStart, single.histMonthEnd), single.contYearStart, single.contYearEnd, getMonthsFromRange(single.contMonthStart, single.contMonthEnd), GeojsonMultiPolygon(listOf(single.polygon.coordinates)), false, multiExcludes, regionUUID = single.regionUUID))
                        }
                        else -> res
                    }
                }
            }
        }

        override fun toFile(file: File, data: ROI) = toFile(adapter, file, data)

        fun getMonthsFromRange(monthStart: Int, monthEnd: Int): List<Int> {
            val months = mutableListOf<Int>()
            if (monthStart > monthEnd) {
                for (x in 1..monthStart) {
                    months.add(x)
                }

                for (y in monthEnd..12) {
                    months.add(y)
                }
            } else {
                for (n in monthStart..monthEnd) {
                    months.add(n)
                }
            }

            return months
        }
    }
}

/**
 * This was split off in prep of release 1.1.5 when support for multi-polygons was added.
 *
 * There is probably a less verbose way of dealing with the overlap here, but this is simple.
 *
 * This class provides backwards compatibility with version 1.1.4 and earlier.
 */
data class SinglePolyROI(
    @Json(name = "buff_dist") val buffDist: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "hist_year_start") val histYearStart: Int = 0,
    @Json(name = "hist_year_end") val histYearEnd: Int = 0,
    @Json(name = "hist_month_start") val histMonthStart: Int = 0,
    @Json(name = "hist_month_end") val histMonthEnd: Int = 0,
    @Json(name = "cont_year_start") val contYearStart: Int = 0,
    @Json(name = "cont_year_end") val contYearEnd: Int = 0,
    @Json(name = "cont_month_start") val contMonthStart: Int = 0,
    @Json(name = "cont_month_end") val contMonthEnd: Int = 0,
    @Json(name = "polygon") val polygon: GeojsonPolygon = GeojsonPolygon(emptyList()),
    @Json(name = "excludes") val excludedRegions: List<GeojsonPolygon> = emptyList(),
    @Json(name = "visualize") val visualize: Boolean = true,
    @Json(name = "region_uuid") val regionUUID: String? = null,
) {
    companion object : Serializer<SinglePolyROI>() {
        private val adapter = make<SinglePolyROI>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: SinglePolyROI) = toFile(adapter, file, data)
    }
}