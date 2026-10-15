package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.blueventures.gemdroid.data.Serializer
import java.io.File

@JsonClass(generateAdapter = true)
data class DynamicsReadyResponse(
    @param:Json(name = "cont_ready") val contReady: Boolean,
    @param:Json(name = "hist_ready") val histReady: Boolean,
    @param:Json(name = "cont_op") val contOp: String,
    @param:Json(name = "hist_op") val histOp: String,
) {
    fun isReady() = contReady && histReady
    companion object : Serializer<DynamicsReadyResponse>() {
        private val adapter = moshiAdapter<DynamicsReadyResponse>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: DynamicsReadyResponse) = toFile(adapter, file, data)
    }
}
