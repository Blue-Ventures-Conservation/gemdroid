package org.blueventures.gemdroid.data.analysis.classification

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import org.blueventures.gemdroid.data.analysis.ImageryExports
import java.io.File

data class ClassificationExports(
    @Json(name = "contemporary") val contemporary: Export,
    @Json(name = "historical") val historical: Export,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object {
        private val adapter = FileService.adapter<ImageryExports>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, exports: ImageryExports) = FileService.toFile(file, exports, adapter)
    }
}
