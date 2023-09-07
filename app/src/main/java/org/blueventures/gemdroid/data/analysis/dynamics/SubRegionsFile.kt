package org.blueventures.gemdroid.data.analysis.dynamics

import com.squareup.moshi.Json
import org.blueventures.gemdroid.data.Serializer
import java.io.File

/**
 * Presence of this file in a target class directory (which is itself a child of the dynamics directory) indicates that
 * sub regions have already been defined for the target class.
 */
data class SubRegionsFile(
    @Json(name = "sub_regions") val subRegions: List<SubRegion>
) {
    companion object : Serializer<SubRegionsFile>() {
        private val adapter = make<SubRegionsFile>()
        override fun fromFile(file: File) = fromFile(adapter, file)
        override fun toFile(file: File, data: SubRegionsFile) = toFile(adapter, file, data)
    }
}
