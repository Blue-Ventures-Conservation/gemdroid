package org.blueventures.gemdroid.data.roi

import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.GeojsonPolygon.Companion.ringFromState
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
    @Json(name = "polygon") val polygon: GeojsonPolygon = GeojsonPolygon(emptyList()),
) {
    fun bounds() = polygon.coordinates[0].map { LatLng(it[1], it[0]) }

    companion object {
        private val adapter = FileService.adapter<ROI>()

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
            points: List<LatLng>,
            buffDist: Int = -1
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
                GeojsonPolygon(listOf(ringFromState(points)))
            )
        }

        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, roi: ROI) = FileService.toFile(file, roi, adapter)
    }
}