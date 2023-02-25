package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class CRA(
    @Json(name = "spatio_temporal_invariant") val spatioTemporalInvariant: Boolean,
    @Json(name = "use_cont_spec") val useContSpec: Boolean,
    @Json(name = "cont_storage_key") val contStorageKey: String,
    @Json(name = "hist_storage_key") val histStorageKey: String,
){
    companion object {
        private val adapter = FileService.adapter<CRA>()

        fun fromFile(file: File): CRA? {
            return FileService.fromFile(file, adapter)
        }

        fun toFile(file: File, cra: CRA): Boolean {
            return FileService.toFile(file, cra, adapter)
        }
    }
}