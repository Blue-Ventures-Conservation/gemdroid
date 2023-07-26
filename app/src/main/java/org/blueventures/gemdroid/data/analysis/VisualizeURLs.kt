package org.blueventures.gemdroid.data.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

// Returned by the backend after receiving an ROI and preparing imagery
data class VisualizeURLs(
    @Json(name = "chot_url") val chotURL: String,
    @Json(name = "clot_url") val clotURL: String,
    @Json(name = "hhot_url") val hhotURL: String,
    @Json(name = "hlot_url") val hlotURL: String,
    @Json(name = "created_at") val createdAt: Int, // seconds
    @Json(name = "timeout") val timeout: Int, // seconds
) {
    fun ordered(i: Int): String {
        return when(i) {
            0 -> chotURL
            1 -> clotURL
            2 -> hhotURL
            3 -> hlotURL
            else -> chotURL
        }
    }

    companion object {
        private val adapter = FileService.adapter<VisualizeURLs>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, urls: VisualizeURLs) = FileService.toFile(file, urls, adapter)
    }
}
