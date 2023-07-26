package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.roi.ROI

// Sent to the backend when requesting classification
data class ClassificationROI(
    @Json(name = "contemporary_storage_key") val contemporaryStorageKey: String,
    @Json(name = "historical_storage_key") val historicalStorageKey: String,
    @Json(name = "use_cont_spec") val useContSpec: Boolean,
    @Json(name = "num_label") val numLabel: String,
    @Json(name = "char_label") val charLabel: String,
    @Json(name = "palette") val palette: List<String>,
    @Json(name = "roi") val roi: ROI
)
