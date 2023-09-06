package org.blueventures.gemdroid.data.analysis

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class TasksResults(
    @Json(name = "results") val results: List<TaskStatus>
) {

    fun completed(): Boolean {
        var res = true
        for (result in results) {
            if (result.error == null && !result.success) {
                res = false
                break
            }
        }
        return res
    }

    companion object {
        private val adapter = FileService.adapter<TasksResults>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, status: TasksResults) = FileService.toFile(file, status, adapter)
    }
}

data class TaskStatus(
    @Json(name = "success") val success: Boolean,
    @Json(name = "error") val error: String?,
) {
    companion object {
        private val adapter = FileService.adapter<TaskStatus>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, status: TaskStatus) = FileService.toFile(file, status, adapter)
    }
}
