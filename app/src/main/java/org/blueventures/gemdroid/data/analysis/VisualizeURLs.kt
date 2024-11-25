package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.data.URLs
import java.io.File

// Returned by the backend after receiving an ROI and preparing imagery
data class VisualizeURLs(
    @Json(name = "chot_url") val chotURL: String,
    @Json(name = "clot_url") val clotURL: String,
    @Json(name = "hhot_url") val hhotURL: String,
    @Json(name = "hlot_url") val hlotURL: String,
    @Json(name = "buff_dist") val buffDist: Int,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): URLs {
    override fun ordered(i: Int): String {
        return when(i) {
            0 -> chotURL
            1 -> clotURL
            2 -> hhotURL
            3 -> hlotURL
            else -> chotURL
        }
    }

    companion object : Serializer<VisualizeURLs>() {
        private val adapter = make<VisualizeURLs>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: VisualizeURLs) = toFile(adapter, file, data)
    }
}
