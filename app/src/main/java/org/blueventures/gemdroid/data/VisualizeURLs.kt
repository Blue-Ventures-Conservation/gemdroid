package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class VisualizeURLs(
    @Json(name = "chot_url") val chotURL: String,
    @Json(name = "clot_url") val clotURL: String,
    @Json(name = "hhot_url") val hhotURL: String,
    @Json(name = "hlot_url") val hlotURL: String,
) {
    companion object {
        fun empty(): VisualizeURLs {
            return VisualizeURLs("", "", "", "")
        }

        fun isEmpty(urls: VisualizeURLs): Boolean {
            return urls.chotURL.isEmpty() && urls.clotURL.isEmpty() && urls.hhotURL.isEmpty() && urls.hlotURL.isEmpty()
        }

        private val adapter = FileService.adapter<VisualizeURLs>()

        fun fromFile(file: File): VisualizeURLs? {
            return FileService.fromFile(file, adapter)
        }

        fun toFile(file: File, urls: VisualizeURLs): Boolean {
            return FileService.toFile(file, urls, adapter)
        }
    }
}
