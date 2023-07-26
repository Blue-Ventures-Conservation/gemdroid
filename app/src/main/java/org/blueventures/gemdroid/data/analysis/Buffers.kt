package org.blueventures.gemdroid.data.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

// the buffers data returned by the server
data class Buffers(
    @Json(name = "sums") val sums: KeysVals,
    @Json(name = "buffers") val buffers: KeysVals
){
    companion object {
        private val adapter = FileService.adapter<Buffers>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, buffer: Buffers) = FileService.toFile(file, buffer, adapter)
    }
}

data class KeysVals(
    @Json(name = "keys") val keys: List<String>,
    @Json(name = "vals") val vals: List<Int>
)