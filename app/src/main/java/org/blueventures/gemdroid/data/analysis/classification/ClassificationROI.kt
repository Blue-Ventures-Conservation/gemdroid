package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.roi.ROI

// Sent to the backend when requesting classification
data class ClassificationROI(
    @param:Json(name = "contemporary_storage_key") val contemporaryStorageKey: String,
    @param:Json(name = "historical_storage_key") val historicalStorageKey: String,
    @param:Json(name = "use_cont_spec") val useContSpec: Boolean,
    @param:Json(name = "num_label") val numLabel: String,
    @param:Json(name = "char_label") val charLabel: String,
    @param:Json(name = "palette") val palette: List<String>,
    @param:Json(name = "roi") val roi: ROI
)
