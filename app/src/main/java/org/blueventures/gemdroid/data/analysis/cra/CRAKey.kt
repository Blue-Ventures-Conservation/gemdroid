package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json

// Sent to the backend when uploading a CRA to GEE
data class CRAKey(
    @Json(name = "key") val key: String
)
