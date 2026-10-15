package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class DrawnPolygonsFile(
    @param:Json(name = "drawn_polygons") val polygons: List<PolygonUtils.NamedPolygon>
) {
    companion object : Serializer<DrawnPolygonsFile>() {
        private val adapter = moshiAdapter<DrawnPolygonsFile>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: DrawnPolygonsFile) = toFile(adapter, file, data)
    }
}
