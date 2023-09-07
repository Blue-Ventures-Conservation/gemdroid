package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.Export
import org.blueventures.gemdroid.data.Serializer
import java.io.File

data class ClassificationExports(
    @Json(name = "contemporary") val contemporary: Export,
    @Json(name = "historical") val historical: Export,
    @Json(name = "created_at") override val createdAt: Int, // seconds
    @Json(name = "timeout") override val timeout: Int, // seconds
): Expires {
    companion object : Serializer<ClassificationExports>() {
        private val adapter = make<ClassificationExports>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: ClassificationExports) = toFile(adapter, file, data)
    }
}
