package org.blueventures.gemdroid.data.analysis.cra

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.roi.ROI

// Sent to the backend when requesting separability charts
data class CraROI(
    @Json(name = "time_period") val timePeriod: Int,
    @Json(name = "storage_key") val key: String,
    @Json(name = "num_label") val numLabel: String,
    @Json(name = "char_label") val charLabel: String,
    @Json(name = "roi") val roi: ROI
)
