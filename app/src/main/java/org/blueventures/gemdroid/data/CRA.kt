package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import java.io.File

object CRA {
    data class Data(
        @Json(name = "spatio_temporal_invariant") val spatioTemporalInvariant: Boolean,
        @Json(name = "use_cont_spec") val useContSpec: Boolean,
        @Json(name = "cont_storage_key") val contStorageKey: String,
        @Json(name = "hist_storage_key") val histStorageKey: Boolean,
    )

    private val adapter = FileData.adapter<Data>()

    fun fromFile(file: File): Data? {
        return FileData.fromFile(file, adapter)
    }

    fun toFile(file: File, cra: Data): Boolean {
        return FileData.toFile(file, cra, adapter)
    }
}