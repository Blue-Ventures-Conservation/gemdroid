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
    @Json(name = "buff_dist") val buffDist: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "cont_year_start") val contYearStart: Int = 0,
    @Json(name = "cont_year_end") val contYearEnd: Int = 0,
    @Json(name = "cont_month_start") val contMonthStart: Int = 0,
    @Json(name = "cont_month_end") val contMonthEnd: Int = 0,
    @Json(name = "hist_year_start") val histYearStart: Int = 0,
    @Json(name = "hist_year_end") val histYearEnd: Int = 0,
    @Json(name = "hist_month_start") val histMonthStart: Int = 0,
    @Json(name = "hist_month_end") val histMonthEnd: Int = 0,
    @Json(name = "polygon") val polygon: GeojsonMultiPolygon = GeojsonMultiPolygon(emptyList()),
    @Json(name = "excludes") val excludedRegions: List<GeojsonMultiPolygon> = emptyList(),
    @Json(name = "visualize") val visualize: Boolean = true,
    @Json(name = "region_uuid") val regionUUID: String? = null,
    @Json(name = "force_landsat") val forceLandsat: Boolean? = null,
) {
    fun boundaryPolyToState() = if (polygon.coordinates.isNotEmpty()) GeojsonMultiPolygon.toState(polygon) else emptyList()

    fun appBarTitle(title: String) = "$name $title"

    fun useS2() = !(forceLandsat ?: true) && shouldUseS2(contYearStart, contMonthStart, histYearStart, histMonthStart)

    companion object : Serializer<ROI>() {
        private val adapter = make<ROI>()

        fun fromState(
            name: String,
            contYearStart: Int,
            contYearEnd: Int,
            contMonthStart: Int,
            contMonthEnd: Int,
            histYearStart: Int,
            histYearEnd: Int,
            histMonthStart: Int,
            histMonthEnd: Int,
            points: MultiPolyPts,
            excludes: List<PolygonDrawer.NamedPolygon>,
            buffDist: Int = -1,
            regionUUID: String? = null,
            forceLandsat: Boolean? = null,
        ): ROI {
            return ROI(
                buffDist,
                name,
                contYearStart,
                contYearEnd,
                contMonthStart,
                contMonthEnd,
                histYearStart,
                histYearEnd,
                histMonthStart,
                histMonthEnd,
                GeojsonMultiPolygon.fromState(points),
                excludes.map { it.polygon },
                regionUUID = regionUUID,
                forceLandsat = forceLandsat,
            )
        }

        override fun fromFile(file: File): Result<ROI> {
            val res = fromFile(adapter, file)
            return when {
                res.isSuccess -> res
                else -> {
                    val singleRes = SinglePolyROI.fromFile(file)
                    when {
                        singleRes.isSuccess -> {
                            val single = singleRes.getOrNull()!!
                            val multi = GeojsonMultiPolygon.toState(GeojsonMultiPolygon(listOf(single.polygon.coordinates)))
                            val multiExcludes = mutableListOf<PolygonDrawer.NamedPolygon>()
                            for (poly in single.excludedRegions) {
                                multiExcludes.add(PolygonDrawer.NamedPolygon("", GeojsonMultiPolygon(listOf(poly.coordinates))))
                            }
                            Result.success(fromState(single.name, single.contYearStart, single.contYearEnd, single.contMonthStart, single.contMonthEnd, single.histYearStart, single.histYearEnd, single.histMonthStart, single.histMonthEnd, multi, multiExcludes, single.buffDist, single.regionUUID))
                        }
                        else -> res
                    }
                }
            }
        }

        override fun toFile(file: File, data: ROI) = toFile(adapter, file, data)
    }
}

/**
 * This was split off in prep of release 1.1.5 when support for multi-polygons was added.
 *
 * Probably a less verbose way of dealing with the overlap here.
 *
 * This class provides backwards compatibility with version 1.1.4 and earlier.
 */
data class SinglePolyROI(
    @Json(name = "buff_dist") val buffDist: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "cont_year_start") val contYearStart: Int = 0,
    @Json(name = "cont_year_end") val contYearEnd: Int = 0,
    @Json(name = "cont_month_start") val contMonthStart: Int = 0,
    @Json(name = "cont_month_end") val contMonthEnd: Int = 0,
    @Json(name = "hist_year_start") val histYearStart: Int = 0,
    @Json(name = "hist_year_end") val histYearEnd: Int = 0,
    @Json(name = "hist_month_start") val histMonthStart: Int = 0,
    @Json(name = "hist_month_end") val histMonthEnd: Int = 0,
    @Json(name = "polygon") val polygon: GeojsonPolygon = GeojsonPolygon(emptyList()),
    @Json(name = "excludes") val excludedRegions: List<GeojsonPolygon> = emptyList(),
    @Json(name = "visualize") val visualize: Boolean = true,
    @Json(name = "region_uuid") val regionUUID: String? = null,
) {
    companion object : Serializer<SinglePolyROI>() {
        private val adapter = SinglePolyROI.make<SinglePolyROI>()
        override fun fromFile(file: File) = SinglePolyROI.fromFile(adapter, file)
        override fun toFile(file: File, data: SinglePolyROI) = SinglePolyROI.toFile(adapter, file, data)
    }
}