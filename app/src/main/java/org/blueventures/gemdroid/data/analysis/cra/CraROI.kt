package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.blueventures.gemdroid.data.roi.ROI

// Sent to the backend when requesting separability charts
@JsonClass(generateAdapter = true)
data class CraROI(
    @param:Json(name = "time_period") val timePeriod: Int,
    @param:Json(name = "storage_key") val key: String,
    @param:Json(name = "num_label") val numLabel: String,
    @param:Json(name = "char_label") val charLabel: String,
    @param:Json(name = "roi") val roi: ROI
)
