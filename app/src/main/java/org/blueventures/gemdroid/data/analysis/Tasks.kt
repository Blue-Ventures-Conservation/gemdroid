package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json

data class Tasks(
    @param:Json(name = "tasks") val tasks: List<String>
)
