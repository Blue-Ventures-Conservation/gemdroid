package org.blueventures.gemdroid.data.analysis.dynamics

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Stale
import java.io.File

data class DynamicsURLs(
    @Json(name = "name") val name: String,
    @Json(name = "stats") val stats: DynamicsStats,
    @Json(name = "loss_url") val lossURL: String,
    @Json(name = "persistence_url") val persistenceURL: String,
    @Json(name = "gain_url") val gainURL: String,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
    @Json(name = "sub_region_stats") val subRegionStats: List<DynamicsStats>,
): Stale {
    fun ordered(i: Int): String {
        return when(i) { 0 -> lossURL; 1 -> persistenceURL; else -> gainURL }
    }

    companion object {
        private val adapter = FileService.adapter<DynamicsURLs>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, urls: DynamicsURLs) = FileService.toFile(file, urls, adapter)
    }
}

data class DynamicsStats(
    @Json(name = "name") val name: String?,
    @Json(name = "contemporary_area") val cont_area: Double,
    @Json(name = "historical_area") val hist_area: Double,
    @Json(name = "loss") val loss: Double,
    @Json(name = "persistence") val persistence: Double,
    @Json(name = "gain") val gain: Double,
)