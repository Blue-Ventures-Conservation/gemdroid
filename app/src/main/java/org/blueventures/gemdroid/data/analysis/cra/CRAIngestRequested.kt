package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json

// Returned by the backend when a table ingestion has been attempted
data class CRAIngestRequested(
    @param:Json(name = "key") val key: String,
    @param:Json(name = "success") val success: Boolean,
    @param:Json(name = "name") val name: String,
)
