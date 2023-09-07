package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import org.blueventures.gemdroid.data.Serializer
import java.io.File

data class ImageryExports(
    @Json(name = "chot") val chot: Export,
    @Json(name = "clot") val clot: Export,
    @Json(name = "hhot") val hhot: Export,
    @Json(name = "hlot") val hlot: Export,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object : Serializer<ImageryExports>() {
        private val adapter = make<ImageryExports>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: ImageryExports) = toFile(adapter, file, data)
    }
}
