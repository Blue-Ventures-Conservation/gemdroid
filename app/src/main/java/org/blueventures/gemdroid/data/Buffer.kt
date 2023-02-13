package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import java.io.File

object Buffer {
    data class Data(
        @Json(name = "buffer_dist") val buffer: Int
    )

    private val adapter = FileData.adapter<Data>()

    fun fromFile(file: File): Data? {
        return FileData.fromFile(file, adapter)
    }

    fun toFile(file: File, buffer: Data): Boolean {
        return FileData.toFile(file, buffer, adapter)
    }
}