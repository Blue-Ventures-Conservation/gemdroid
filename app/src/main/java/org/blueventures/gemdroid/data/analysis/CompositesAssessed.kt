package org.blueventures.gemdroid.data.analysis

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.blueventures.gemdroid.data.Serializer
import java.io.File

// existence is all that really matters about this file
// it is used to show that the false color composites have been assessed and accepted
@JsonClass(generateAdapter = true)
data class CompositesAssessed(
    @param:Json(name = "assessed") val assessed: Boolean
){
    companion object : Serializer<CompositesAssessed>() {
        private val adapter = moshiAdapter<CompositesAssessed>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: CompositesAssessed) = toFile(adapter, file, data)
    }
}
