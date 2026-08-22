package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.data.roi.ROI

data class DynamicsROI(
    @param:Json(name = "target_classes") val targetClass: List<String>,
    @param:Json(name = "combined_name") val combinedName: String?,
    @param:Json(name = "sub_regions") val subRegions: List<PolygonUtils.NamedPolygon>,
    @param:Json(name = "red") val loss: String,
    @param:Json(name = "blue") val persistence: String,
    @param:Json(name = "green") val gain: String,
    @param:Json(name = "contemporary_storage_key") val contemporaryStorageKey: String,
    @param:Json(name = "historical_storage_key") val historicalStorageKey: String,
    @param:Json(name = "use_cont_spec") val useContSpec: Boolean,
    @param:Json(name = "num_label") val numLabel: String,
    @param:Json(name = "char_label") val charLabel: String,
    @param:Json(name = "roi") val roi: ROI
)