package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.data.roi.ROI

data class DynamicsROI(
    @Json(name = "target_classes") val targetClass: List<String>,
    @Json(name = "combined_name") val combinedName: String?,
    @Json(name = "sub_regions") val subRegions: List<PolygonUtils.NamedPolygon>,
    @Json(name = "red") val loss: String,
    @Json(name = "blue") val persistence: String,
    @Json(name = "green") val gain: String,
    @Json(name = "contemporary_storage_key") val contemporaryStorageKey: String,
    @Json(name = "historical_storage_key") val historicalStorageKey: String,
    @Json(name = "use_cont_spec") val useContSpec: Boolean,
    @Json(name = "num_label") val numLabel: String,
    @Json(name = "char_label") val charLabel: String,
    @Json(name = "roi") val roi: ROI
)