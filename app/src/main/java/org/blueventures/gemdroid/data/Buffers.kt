package org.blueventures.gemdroid.data

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

        fun empty(): Buffers {
            return Buffers(KeysVals(emptyList(), emptyList()), KeysVals(emptyList(), emptyList()))
        }

        fun isEmpty(buffers: Buffers?): Boolean {
            val b = buffers ?: return true
            return b.sums.keys.isEmpty() && b.sums.vals.isEmpty() && b.buffers.keys.isEmpty() && b.buffers.vals.isEmpty()
        }

        private val adapter = FileData.adapter<Buffers>()

        fun fromFile(file: File): Buffers? {
            return FileData.fromFile(file, adapter)
        }

        fun toFile(file: File, buffer: Buffers): Boolean {
            return FileData.toFile(file, buffer, adapter)
        }
    }
}