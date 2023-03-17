package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class Success(
    @Json(name = "success") val success: Boolean
)
