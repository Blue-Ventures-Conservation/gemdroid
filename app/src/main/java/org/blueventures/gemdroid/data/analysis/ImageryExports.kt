package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import org.blueventures.gemdroid.data.Serializer
import java.io.File

@JsonClass(generateAdapter = true)
data class ImageryExports(
    @param:Json(name = "chot") val chot: Export,
    @param:Json(name = "clot") val clot: Export,
    @param:Json(name = "hhot") val hhot: Export,
    @param:Json(name = "hlot") val hlot: Export,
    @param:Json(name = "created_at") override val createdAt: Int, // seconds
    @param:Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object : Serializer<ImageryExports>() {
        private val adapter = moshiAdapter<ImageryExports>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: ImageryExports) = toFile(adapter, file, data)
    }
}
