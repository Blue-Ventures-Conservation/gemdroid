package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Sent to the backend when checking on a previously started CRA table ingestion
@JsonClass(generateAdapter = true)
data class UploadName(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "key") val key: String,
)
