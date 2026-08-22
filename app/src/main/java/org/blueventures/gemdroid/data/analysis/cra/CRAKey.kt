package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json

// Sent to the backend when uploading a CRA to GEE
data class CRAKey(
    @param:Json(name = "key") val key: String,
    @param:Json(name = "is_shapefile") val isShapefile: Boolean,
    @param:Json(name = "overwrite") val overwrite: Boolean,
)
