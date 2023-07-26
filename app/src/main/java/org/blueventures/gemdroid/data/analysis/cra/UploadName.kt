package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json

// Sent to the backend when checking on a previously started CRA table ingestion
data class UploadName(
    @Json(name = "name") val name: String,
    @Json(name = "key") val key: String,
)
