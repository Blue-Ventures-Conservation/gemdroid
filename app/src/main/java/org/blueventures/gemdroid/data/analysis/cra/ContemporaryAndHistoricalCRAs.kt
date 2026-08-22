package org.blueventures.gemdroid.data.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

/**
 * If the historical Shapefile is null, then we use the contemporary trained
 * classifier on historical imagery on the backend. Otherwise, the historical
 * Shapefile can point to the same CRA as the contemporary, which implies the
 * reference areas are spatio-temporally invariant.
 *
 * A CRA file is saved on device to keep track of the CRAs in use for an ROI.
 */
data class ContemporaryAndHistoricalCRAs(
    @param:Json(name = "hist_shp") val historicalCRA: RemoteCRAFileInfo? = null,
    @param:Json(name = "cont_shp") val contemporaryCRA: RemoteCRAFileInfo,
) {
    companion object {
        private val adapter = FileService.adapter<ContemporaryAndHistoricalCRAs>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, cras: ContemporaryAndHistoricalCRAs) = FileService.toFile(file, cras, adapter)
    }

    fun historicalShp() = historicalCRA ?: contemporaryCRA
    fun useContSpec() = historicalCRA == null
}