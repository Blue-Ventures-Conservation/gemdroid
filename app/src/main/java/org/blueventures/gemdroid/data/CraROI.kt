package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class CraROI(
    @Json(name = "storage_key") val key: String,
    @Json(name = "num_label") val numLabel: String,
    @Json(name = "char_label") val charLabel: String,
    @Json(name = "roi") val roi: ROI
)
