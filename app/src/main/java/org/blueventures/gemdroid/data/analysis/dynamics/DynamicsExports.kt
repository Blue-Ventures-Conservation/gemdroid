package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import org.blueventures.gemdroid.data.Serializer
import java.io.File

data class DynamicsExports(
    @Json(name = "loss") val loss: Export,
    @Json(name = "persistence") val persistence: Export,
    @Json(name = "gain") val gain: Export,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object : Serializer<DynamicsExports>() {
        private val adapter = make<DynamicsExports>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: DynamicsExports) = toFile(adapter, file, data)
    }
}
