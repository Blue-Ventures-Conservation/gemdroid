package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class Buffers(
    @Json(name = "sums") val sums: KeysVals,
    @Json(name = "buffers") val buffers: KeysVals
){
    companion object {
        data class KeysVals(
            @Json(name = "keys") val keys: List<String>,
            @Json(name = "vals") val vals: List<Int>
        )

        private val adapter = FileService.adapter<Buffers>()

        fun fromFile(file: File): Result<Buffers> {
            return FileService.fromFile(file, adapter)
        }

        fun toFile(file: File, buffer: Buffers): Result<Unit> {
            return FileService.toFile(file, buffer, adapter)
        }
    }
}