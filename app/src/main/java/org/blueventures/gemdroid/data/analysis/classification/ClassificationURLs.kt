package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.data.URLs
import java.io.File

// returned by the backend when requesting classification, saved to disk
data class ClassificationURLs(
    @Json(name = "contemporary_classification") val contemporaryClassification: Classified,
    @Json(name = "historical_classification") val historicalClassification: Classified,
    @Json(name = "classes") val classes: List<String>,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): URLs {
    override fun ordered(i: Int): String {
        return when(i) { 0 -> contemporaryClassification.url; 1 -> historicalClassification.url; else -> contemporaryClassification.url }
    }

    companion object : Serializer<ClassificationURLs>() {
        private val adapter = make<ClassificationURLs>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: ClassificationURLs) = toFile(adapter, file, data)
    }
}

data class Classified(
    @Json(name = "url") val url: String,
    @Json(name = "resubstitution_accuracy") val resubstitutionAccuracy: Float,
    @Json(name = "validation_accuracy") val validationAccuracy: Float,
)