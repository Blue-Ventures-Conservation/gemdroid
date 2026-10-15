package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tasks(
    @param:Json(name = "tasks") val tasks: List<String>
)
