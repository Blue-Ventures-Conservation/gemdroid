package org.blueventures.gemdroid.data.analysis.dynamics

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class DynamicsURLs(
    @Json(name = "gain_url") val gainURL: String,
    @Json(name = "loss_url") val lossURL: String,
    @Json(name = "persistence_url") val persistenceURL: String,
    @Json(name = "stats") val stats: List<Float>,
    @Json(name = "created_at") val createdAt: Int, // seconds
    @Json(name = "timeout") val timeout: Int, // seconds
) {
    fun ordered(i: Int): String {
        return when(i) { 0 -> gainURL; 1 -> lossURL; else -> persistenceURL }
    }

    companion object {
        private val adapter = FileService.adapter<DynamicsURLs>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, urls: DynamicsURLs) = FileService.toFile(file, urls, adapter)
    }
}