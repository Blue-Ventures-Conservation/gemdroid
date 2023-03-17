package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

// this is only used to indicate by the presence of a file
// that the buffer has been selected
data class Buffer(
    @Json(name = "buffer_dist") val buffer: Int
){
    companion object {
        private val adapter = FileService.adapter<Buffer>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, buffer: Buffer) = FileService.toFile(file, buffer, adapter)
    }
}