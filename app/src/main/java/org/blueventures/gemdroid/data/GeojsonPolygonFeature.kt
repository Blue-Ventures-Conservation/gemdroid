package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.analysis.cra.ClassCount
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classNamePropertyKey
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classNumberPropertyKey
import java.io.File

data class GeojsonPolygonFeature(
    @Json(name = "type") val type: String = "Feature",
    @Json(name = "geometry") val geometry: GeojsonPolygon,
    @Json(name = "properties") var properties: Map<String, Any>
)

data class GeojsonPolygonFeatureCollection(
    @Json(name = "type") val type: String = "FeatureCollection",
    @Json(name = "features") val features: List<GeojsonPolygonFeature>
) {
    // Int properties are written to disk without decimals, but when read back
    // the JsonAdapter interprets unstructured JSON values as Doubles
    fun fixFloats() {
        for (feature in features) {
            val newMap = mutableMapOf<String, Any>()
            feature.properties.forEach { (key, value) ->
                if (key == classNumberPropertyKey) {
                    val newVal = (value as Double).toInt()
                    newMap[key] = newVal
                } else {
                    newMap[key] = value
                }
            }
            feature.properties = newMap
        }
    }

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
            val classNumber = feature.properties[classNumberPropertyKey] as? Int
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