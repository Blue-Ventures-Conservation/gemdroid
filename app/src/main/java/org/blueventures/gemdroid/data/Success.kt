package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

// Returned from the backend when awaiting results of a CRA table ingestion
data class Success(
    @Json(name = "success") val success: Boolean
)
