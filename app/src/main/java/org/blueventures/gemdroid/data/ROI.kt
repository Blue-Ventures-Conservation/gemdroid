package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import org.blueventures.gemdroid.model.roi.RoiState
import java.io.File

data class ROI(
    @Json(name = "buff_dist") val buffDist: Int,
    @Json(name = "name") val name: String,
    @Json(name = "cont_year_start") val contYearStart: Int,
    @Json(name = "cont_year_end") val contYearEnd: Int,
    @Json(name = "hist_year_start") val histYearStart: Int,
    @Json(name = "hist_year_end") val histYearEnd: Int,
    @Json(name = "month_start") val monthStart: Int,
    @Json(name = "month_end") val monthEnd: Int,
    @Json(name = "indices") val indices: List<String>,
    @Json(name = "polygon") val polygon: Polygon,
){
    companion object {
        data class Polygon(
            @Json(name = "type") val type: String,
            @Json(name = "coordinates") val coordinates: List<List<List<Double>>>,
        )

        private val adapter = FileData.adapter<ROI>()

        fun fromState(state: RoiState, buffDist: Int = -1): ROI {
            return ROI(
                buffDist,
                state.name,
                state.contemporaryYearStart,
                state.contemporaryYearEnd,
                state.historicalYearStart,
                state.historicalYearEnd,
                state.monthStart,
                state.monthEnd,
                state.indices.toList(),
                polygonFromState(state)
            )
        }

        fun fromFile(file: File): ROI? {
            return FileData.fromFile(file, adapter)
        }

        fun toFile(file: File, roi: ROI): Boolean {
            return FileData.toFile(file, roi, adapter)
        }

        private fun polygonFromState(state: RoiState): Polygon {
            val coordinates: ArrayList<List<Double>> = arrayListOf()
            for (point in state.points) {
                coordinates.add(listOf(point.longitude, point.latitude))
            }

            val first = state.points.first()
            if (first != state.points.last()) {
                coordinates.add(listOf(first.longitude, first.latitude))
            }

            return Polygon(
                "Polygon",
                listOf(coordinates.toList())
            )
        }
    }
}