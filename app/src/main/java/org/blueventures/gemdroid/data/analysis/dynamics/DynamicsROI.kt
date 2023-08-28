package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.roi.ROI

data class DynamicsROI(
    @Json(name = "target_class") val targetClass: String,
    @Json(name = "sub_regions") val subRegions: List<SubRegion>,
    @Json(name = "red") val red: String,
    @Json(name = "green") val green: String,
    @Json(name = "blue") val blue: String,
    @Json(name = "contemporary_storage_key") val contemporaryStorageKey: String,
    @Json(name = "historical_storage_key") val historicalStorageKey: String,
    @Json(name = "use_cont_spec") val useContSpec: Boolean,
    @Json(name = "num_label") val numLabel: String,
    @Json(name = "char_label") val charLabel: String,
    @Json(name = "roi") val roi: ROI
)

data class SubRegion(
    @Json(name = "name") val name: String,
    @Json(name = "geometry") val polygon: GeojsonPolygon,
)
