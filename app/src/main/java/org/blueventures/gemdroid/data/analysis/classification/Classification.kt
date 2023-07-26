package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json

// returned by the backend when requesting classification, saved to disk
data class Classification(
    @Json(name = "contemporary_classification") val contemporaryClassification: Classified,
    @Json(name = "historical_classification") val historicalClassification: Classified,
    @Json(name = "classes") val classes: List<String>,
    @Json(name = "created_at") val createdAt: Int, // seconds
    @Json(name = "timeout") val timeout: Int, // seconds
)

data class Classified(
    @Json(name = "url") val url: String,
    @Json(name = "resubstitution_accuracy") val resubstitutionAccuracy: Float,
    @Json(name = "validation_accuracy") val validationAccuracy: Float,
)