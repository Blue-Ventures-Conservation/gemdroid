package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class CRAUploadResult(
    @Json(name = "key") val key: String,
    @Json(name = "success") val success: Boolean,
    @Json(name = "name") val name: String,
)
