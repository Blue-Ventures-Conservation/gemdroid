package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import java.io.File

data class CRA(
    @Json(name = "spatio_temporal_invariant") val spatioTemporalInvariant: Boolean,
    @Json(name = "use_cont_spec") val useContSpec: Boolean,
    @Json(name = "cont_storage_key") val contStorageKey: String,
    @Json(name = "hist_storage_key") val histStorageKey: Boolean,
){
    companion object {
        private val adapter = FileData.adapter<CRA>()

        fun fromFile(file: File): CRA? {
            return FileData.fromFile(file, adapter)
        }

        fun toFile(file: File, cra: CRA): Boolean {
            return FileData.toFile(file, cra, adapter)
        }
    }
}