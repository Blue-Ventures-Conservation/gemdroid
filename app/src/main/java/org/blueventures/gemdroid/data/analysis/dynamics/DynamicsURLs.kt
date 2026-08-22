package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.data.URLs
import java.io.File

data class DynamicsURLs(
    @param:Json(name = "loss_url") val lossURL: String,
    @param:Json(name = "persistence_url") val persistenceURL: String,
    @param:Json(name = "gain_url") val gainURL: String,
    @param:Json(name = "stats") val stats: RegionStats,
    @param:Json(name = "sub_region_stats") val subRegionStats: List<RegionStats>,
    @param:Json(name = "created_at") override val createdAt: Int, // seconds
    @param:Json(name = "timeout") override val timeout: Int, // seconds
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
    @param:Json(name = "name") val name: String,
    @param:Json(name = "all_classes") val allClasses: List<ClassDynamics>,
)

data class ClassDynamics(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "cont") val contArea: Double,
    @param:Json(name = "hist") val histArea: Double,
    @param:Json(name = "loss") val loss: Double,
    @param:Json(name = "persistence") val persistence: Double,
    @param:Json(name = "gain") val gain: Double,
    @param:Json(name = "conversions") val conversions: ClassConversions,
)

data class ClassConversions(
    @param:Json(name = "to") val to: List<Conversion>,
    @param:Json(name = "from") val from: List<Conversion>,
)

data class Conversion(
    @param:Json(name = "area") val area: Double,
    @param:Json(name = "name") val name: String,
)