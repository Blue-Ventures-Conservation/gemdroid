package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.data.URLs
import java.io.File

data class DynamicsURLs(
    @Json(name = "loss_url") val lossURL: String,
    @Json(name = "persistence_url") val persistenceURL: String,
    @Json(name = "gain_url") val gainURL: String,
    @Json(name = "stats") val stats: RegionStats,
    @Json(name = "sub_region_stats") val subRegionStats: List<RegionStats>,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): URLs {
    override fun ordered(i: Int): String {
        return when(i) { 0 -> lossURL; 1 -> persistenceURL; else -> gainURL }
    }

    companion object : Serializer<DynamicsURLs>() {
        private val adapter = make<DynamicsURLs>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: DynamicsURLs) = toFile(adapter, file, data)
    }
}

data class RegionStats(
    @Json(name = "name") val name: String,
    @Json(name = "all_classes") val allClasses: List<ClassDynamics>,
)

data class ClassDynamics(
    @Json(name = "name") val name: String,
    @Json(name = "cont") val contArea: Double,
    @Json(name = "hist") val histArea: Double,
    @Json(name = "loss") val loss: Double,
    @Json(name = "persistence") val persistence: Double,
    @Json(name = "gain") val gain: Double,
    @Json(name = "conversions") val conversions: ClassConversions,
)

data class ClassConversions(
    @Json(name = "to") val to: List<Conversion>,
    @Json(name = "from") val from: List<Conversion>,
)

data class Conversion(
    @Json(name = "area") val area: Double,
    @Json(name = "name") val name: String,
)