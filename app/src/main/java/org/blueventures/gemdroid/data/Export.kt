package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class Export(
    @Json(name = "task") val name: String,
    @Json(name = "path") val storagePath: String,
)
