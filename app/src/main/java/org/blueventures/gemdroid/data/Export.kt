package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class Export(
    @param:Json(name = "task") val name: String,
    @param:Json(name = "path") val storagePath: String,
)
