package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

// Returned by the backend after receiving an ROI and preparing imagery
data class VisualizeURLs(
    @Json(name = "chot_url") val chotURL: String,
    @Json(name = "clot_url") val clotURL: String,
    @Json(name = "hhot_url") val hhotURL: String,
    @Json(name = "hlot_url") val hlotURL: String,
) {
    companion object {
        private val adapter = FileService.adapter<VisualizeURLs>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, urls: VisualizeURLs) = FileService.toFile(file, urls, adapter)
    }
}
