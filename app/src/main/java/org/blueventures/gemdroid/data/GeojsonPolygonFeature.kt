package org.blueventures.gemdroid.data

import com.squareup.moshi.Json

data class GeojsonPolygonFeature(
    @Json(name = "type") val type: String = "Feature",
    @Json(name = "geometry") val geometry: GeojsonPolygon,
    @Json(name = "properties") val properties: Map<String, Any>
)

data class GeojsonPolygonFeatureCollection(
    @Json(name = "type") val type: String = "FeatureCollection",
    @Json(name = "features") val features: List<GeojsonPolygonFeature>
) {
    fun intPropertyCount(property: String, target: Int): Int {
        var total = 0
        for (feature in features) {
            (feature.properties[property] as? Int)?.let { value ->
                if (value == target) {
                    total += 1
                }
            }
        }

        return total
    }
}
