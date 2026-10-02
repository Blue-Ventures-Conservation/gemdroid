package org.blueventures.gemdroid.data.analysis.classification

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import java.io.File

data class ClassificationReadyResponse(
    @param:Json(name = "chot_ready") val chotReady: Boolean,
    @param:Json(name = "clot_ready") val clotReady: Boolean,
    @param:Json(name = "hhot_ready") val hhotReady: Boolean,
    @param:Json(name = "hlot_ready") val hlotReady: Boolean,
    @param:Json(name = "chot_op") val chotOp: String,
    @param:Json(name = "clot_op") val clotOp: String,
    @param:Json(name = "hhot_op") val hhotOp: String,
    @param:Json(name = "hlot_op") val hlotOp: String,
) {
    fun isReady() = chotReady && clotReady && hhotReady && hlotReady
    companion object : Serializer<ClassificationReadyResponse>() {
        private val adapter = make<ClassificationReadyResponse>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: ClassificationReadyResponse) = toFile(adapter, file, data)
    }
}
