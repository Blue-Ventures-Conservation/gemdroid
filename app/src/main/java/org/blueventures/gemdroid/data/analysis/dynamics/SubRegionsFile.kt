package org.blueventures.gemdroid.data.analysis.dynamics

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

/**
 * Presence of this file in a target class directory (which is itself a child of the dynamics directory) indicates that
 * sub regions have already been defined for the target class.
 */
data class SubRegionsFile(
    @Json(name = "sub_regions") val subRegions: List<SubRegion>
) {
    companion object {
        private val adapter = FileService.adapter<SubRegionsFile>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, regions: SubRegionsFile) = FileService.toFile(file, regions, adapter)
    }
}
