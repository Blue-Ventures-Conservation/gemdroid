package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import java.io.File

data class Buffer(
    @Json(name = "buffer_dist") val buffer: Int
){
    companion object {
        private val adapter = FileData.adapter<Buffer>()

        fun fromFile(file: File): Buffer? {
            return FileData.fromFile(file, adapter)
        }

        fun toFile(file: File, buffer: Buffer): Boolean {
            return FileData.toFile(file, buffer, adapter)
        }
    }
}