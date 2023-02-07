package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.blueventures.gemdroid.model.roi.RoiState
import java.io.File

data class ROI(
    @field:Json(name = "buff_dist") val buffDist: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "cont_year_start") val contYearStart: Int,
    @field:Json(name = "cont_year_end") val contYearEnd: Int,
    @field:Json(name = "hist_year_start") val histYearStart: Int,
    @field:Json(name = "hist_year_end") val histYearEnd: Int,
    @field:Json(name = "month_start") val monthStart: Int,
    @field:Json(name = "month_end") val monthEnd: Int,
    @field:Json(name = "indices") val indices: List<String>,
    @field:Json(name = "polygon") val polygon: Polygon,
)

data class Polygon(
    @field:Json(name = "type") val type: String,
    @field:Json(name = "coordinates") val coordinates: List<List<List<Double>>>,
)

object ROIBuilder {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(ROI::class.java)

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
        return try {
            val json = file.bufferedReader().use { it.readText() }
            adapter.fromJson(json)
        } catch(e: Exception) {
            null
        }
    }

    fun toFile(file: File, roi: ROI): Boolean {
        return try {
            file.writeText(adapter.toJson(roi))
            true
        } catch(e: Exception) {
            false
        }
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