package org.blueventures.gemdroid.data.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

// Returned from the backend when awaiting results of a CRA table ingestion
data class Success(
    @Json(name = "success") val success: Boolean
) {
    fun ingestNeeded() = !success

    companion object {
        private val adapter = FileService.adapter<Success>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, success: Success) = FileService.toFile(file, success, adapter)
    }
}
