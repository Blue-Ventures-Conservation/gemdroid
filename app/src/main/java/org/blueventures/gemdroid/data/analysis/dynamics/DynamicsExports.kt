package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import org.blueventures.gemdroid.data.Serializer
import java.io.File

data class DynamicsExports(
    @param:Json(name = "csv") val csv: Export?,
    @param:Json(name = "loss") val loss: Export,
    @param:Json(name = "persistence") val persistence: Export,
    @param:Json(name = "gain") val gain: Export,
    @param:Json(name = "created_at") override val createdAt: Int, // seconds
    @param:Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object : Serializer<DynamicsExports>() {
        private val adapter = make<DynamicsExports>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: DynamicsExports) = toFile(adapter, file, data)
    }
}
