package org.blueventures.gemdroid.data.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import java.io.File

data class ImageryExports(
    @Json(name = "chot") val chot: Export,
    @Json(name = "clot") val clot: Export,
    @Json(name = "hhot") val hhot: Export,
    @Json(name = "hlot") val hlot: Export,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object {
        private val adapter = FileService.adapter<ImageryExports>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, exports: ImageryExports) = FileService.toFile(file, exports, adapter)
    }
}
