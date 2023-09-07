package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import java.io.File

// this is only used to indicate by the presence of a file
// that the buffer has been selected
data class Buffer(
    @Json(name = "buffer_dist") val buffer: Int
){
    companion object : Serializer<Buffer>() {
        private val adapter = make<Buffer>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: Buffer) = toFile(adapter, file, data)
    }
}