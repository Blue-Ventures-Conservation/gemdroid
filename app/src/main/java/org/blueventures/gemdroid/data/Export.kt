package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Export(
    @param:Json(name = "task") val name: String,
    @param:Json(name = "path") val storagePath: String,
)
