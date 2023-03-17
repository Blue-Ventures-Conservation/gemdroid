package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class UploadName(
    @Json(name = "name") val name: String,
    @Json(name = "key") val key: String,
)
