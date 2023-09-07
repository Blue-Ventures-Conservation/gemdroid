package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import java.io.File

// the buffers data returned by the server
data class Buffers(
    @Json(name = "sums") val sums: KeysVals,
    @Json(name = "buffers") val buffers: KeysVals
){
    companion object : Serializer<Buffers>() {
        private val adapter = make<Buffers>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: Buffers) = toFile(adapter, file, data)
    }
}

data class KeysVals(
    @Json(name = "keys") val keys: List<String>,
    @Json(name = "vals") val vals: List<Int>
)