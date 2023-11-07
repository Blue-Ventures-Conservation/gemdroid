package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import java.io.File

data class DrawnPolygonsFile(
    @Json(name = "drawn_polygons") val polygons: List<PolygonDrawer.NamedPolygon>
) {
    companion object : Serializer<DrawnPolygonsFile>() {
        private val adapter = make<DrawnPolygonsFile>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: DrawnPolygonsFile) = toFile(adapter, file, data)
    }
}
