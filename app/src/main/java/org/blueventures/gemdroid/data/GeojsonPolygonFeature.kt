package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class GeojsonPolygonFeature(
    @Json(name = "type") val type: String = "Feature",
    @Json(name = "geometry") val geometry: GeojsonPolygon,
    @Json(name = "properties") val properties: Map<String, Any>
)
