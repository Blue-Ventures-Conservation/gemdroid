package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json
import java.io.File

data class ROI(
    @Json(name = "buff_dist") val buffDist: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "cont_year_start") val contYearStart: Int = 0,
    @Json(name = "cont_year_end") val contYearEnd: Int = 0,
    @Json(name = "hist_year_start") val histYearStart: Int = 0,
    @Json(name = "hist_year_end") val histYearEnd: Int = 0,
    @Json(name = "month_start") val monthStart: Int = 0,
    @Json(name = "month_end") val monthEnd: Int = 0,
    @Json(name = "indices") val indices: List<String> = emptyList(),
    @Json(name = "polygon") val polygon: Polygon = Polygon(),
){
    companion object {
        private val adapter = FileService.adapter<ROI>()

        fun fromState(
            name: String,
            contYearStart: Int,
            contYearEnd: Int,
            histYearStart: Int,
            histYearEnd: Int,
            monthStart: Int,
            monthEnd: Int,
            indices: List<String>,
            points: List<LatLng>,
            buffDist: Int = -1
        ): ROI {
            return ROI(
                buffDist,
                name,
                contYearStart,
                contYearEnd,
                histYearStart,
                histYearEnd,
                monthStart,
                monthEnd,
                indices,
                polygonFromState(points)
            )
        }

        fun fromFile(file: File): Result<ROI> {
            return FileService.fromFile(file, adapter)
        }

        fun toFile(file: File, roi: ROI): Result<Unit> {
            return FileService.toFile(file, roi, adapter)
        }

        private fun polygonFromState(points: List<LatLng>): Polygon {
            val coordinates: ArrayList<List<Double>> = arrayListOf()
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