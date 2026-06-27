package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.analysis.cra.ClassCount
import java.io.File

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

    fun countClasses(): List<ClassCount> {
        val counts = mutableMapOf<String, ClassCount>()
        for (feature in features) {
            val className = feature.properties[classNamePropertyKey] as? String
            val classNumber = feature.properties[classNamePropertyKey] as? Int
            if (className != null && classNumber != null) {
                if (counts[className] == null) {
                    counts[className] = ClassCount(className, classNumber, 0)
                }

                counts[className]!!.craCount += 1
            }
        }

        return counts.values.sortedBy { it.classNumber }
    }

    companion object : Serializer<GeojsonPolygonFeatureCollection>() {
        private val adapter = make<GeojsonPolygonFeatureCollection>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: GeojsonPolygonFeatureCollection) = toFile(adapter, file, data)
    }
}

const val classNamePropertyKey = "classname"
const val classNumberPropertyKey = "classnumber"