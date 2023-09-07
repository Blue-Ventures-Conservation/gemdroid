package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
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

    companion object : Serializer<TasksResults>() {
        private val adapter = make<TasksResults>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: TasksResults) = toFile(adapter, file, data)
    }
}

data class TaskStatus(
    @Json(name = "success") val success: Boolean,
    @Json(name = "error") val error: String?,
)
