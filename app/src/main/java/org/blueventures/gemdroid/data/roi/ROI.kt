package org.blueventures.gemdroid.data.roi

import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json
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
    @Json(name = "polygon") val polygon: Polygon = Polygon(),
) {
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
                polygonFromState(points)
            )
        }

        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, roi: ROI) = FileService.toFile(file, roi, adapter)

        private fun polygonFromState(points: List<LatLng>): Polygon {
            val coordinates = mutableListOf<List<Double>>()
            for (point in points) {
                coordinates.add(listOf(point.longitude, point.latitude))
            }

            val first = points.first()
            if (first != points.last()) {
                coordinates.add(listOf(first.longitude, first.latitude))
            }

            return Polygon(
                coordinates = listOf(coordinates.toList())
            )
        }
    }
}

data class Polygon(
    @Json(name = "type") val type: String = "Polygon",
    @Json(name = "coordinates") val coordinates: List<List<List<Double>>> = emptyList(),
)