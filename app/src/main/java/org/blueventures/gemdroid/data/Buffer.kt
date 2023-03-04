package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class Buffer(
    @Json(name = "buffer_dist") val buffer: Int
){
    companion object {
        private val adapter = FileService.adapter<Buffer>()

        fun fromFile(file: File): Result<Buffer> {
            return FileService.fromFile(file, adapter)
        }

        fun toFile(file: File, buffer: Buffer): Result<Unit> {
            return FileService.toFile(file, buffer, adapter)
        }
    }
}