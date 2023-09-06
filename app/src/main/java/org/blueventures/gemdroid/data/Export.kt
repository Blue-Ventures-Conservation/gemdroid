package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class Export(
    @Json(name = "task") val name: String,
    @Json(name = "path") val storagePath: String,
) {
    companion object {
        private val adapter = FileService.adapter<Export>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, export: Export) = FileService.toFile(file, export, adapter)
    }
}
