package org.blueventures.gemdroid.data.analysis.dynamics

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import java.io.File

data class DynamicsExports(
    @Json(name = "loss") val loss: Export,
    @Json(name = "persistence") val persistence: Export,
    @Json(name = "gain") val gain: Export,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object {
        private val adapter = FileService.adapter<DynamicsExports>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, exports: DynamicsExports) = FileService.toFile(file, exports, adapter)
    }
}
