package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.roi.ROI

data class ClassificationReady(
    @param:Json(name = "contemporary_high_tide_op") val chotOp: String,
    @param:Json(name = "contemporary_low_tide_op") val clotOp: String,
    @param:Json(name = "historical_high_tide_op") val hhotOp: String,
    @param:Json(name = "historical_low_tide_op") val hlotOp: String,
    @param:Json(name = "roi") val roi: ROI,
)
