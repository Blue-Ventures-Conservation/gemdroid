package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json

data class Tasks(
    @Json(name = "tasks") val tasks: List<String>
)
