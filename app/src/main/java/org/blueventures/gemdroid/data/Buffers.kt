package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import java.io.File

object Buffers {
    data class Data(
        @Json(name = "sums") val sums: KeysVals,
        @Json(name = "buffers") val buffers: KeysVals
    )

    data class KeysVals(
        @Json(name = "keys") val keys: List<String>,
        @Json(name = "vals") val vals: List<Int>
    )

    fun empty(): Data {
        return Data(KeysVals(emptyList(), emptyList()), KeysVals(emptyList(), emptyList()))
    }

    fun isEmpty(buffers: Data?): Boolean {
        val b = buffers ?: return true
        return b.sums.keys.isEmpty() && b.sums.vals.isEmpty() && b.buffers.keys.isEmpty() && b.buffers.vals.isEmpty()
    }

    private val adapter = FileData.adapter<Data>()

    fun fromFile(file: File): Data? {
        return FileData.fromFile(file, adapter)
    }

    fun toFile(file: File, buffer: Data): Boolean {
        return FileData.toFile(file, buffer, adapter)
    }
}