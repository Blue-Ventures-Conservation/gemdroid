package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CRAClass(
    @param:Json(name = "number") val number: Int,
    @param:Json(name = "name") val name: String
)