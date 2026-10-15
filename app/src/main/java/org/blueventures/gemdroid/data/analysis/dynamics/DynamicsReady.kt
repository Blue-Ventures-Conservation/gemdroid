package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.blueventures.gemdroid.data.roi.ROI

@JsonClass(generateAdapter = true)
data class DynamicsReady(
    @param:Json(name = "contemporary_classification_image_op") val contImageOp: String,
    @param:Json(name = "historical_classification_image_op") val histImageOp: String,
    @param:Json(name = "contemporary_storage_key") val contemporaryStorageKey: String,
    @param:Json(name = "historical_storage_key") val historicalStorageKey: String,
    @param:Json(name = "use_cont_spec") val useContSpec: Boolean,
    @param:Json(name = "num_label") val numLabel: String,
    @param:Json(name = "char_label") val charLabel: String,
    @param:Json(name = "roi") val roi: ROI,
)