package org.blueventures.gemdroid.data

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
data class CRA(
    @Json(name = "cont_shp") val contemporaryCRA: Shapefile,
    @Json(name = "hist_shp") val historicalCRA: Shapefile? = null,
) {
    companion object {
        private val adapter = FileService.adapter<CRA>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, cra: CRA) = FileService.toFile(file, cra, adapter)
    }

    fun historicalShp() = historicalCRA ?: contemporaryCRA
}

// Saved locally, and also saved in Cloud Storage alongside the zip to keep track of
// the parsed/selected fields used for analysis
data class Shapefile(
    @Json(name = "shp_storage_key") val shapefileStorageKey: String,
    @Json(name = "table_upload_operation_name") val tableUploadOperationName: String,
    @Json(name = "numeric_class_field") val numericClassField: String,
    @Json(name = "string_class_field") val stringClassField: String,
    @Json(name = "string_class_field_values") val stringClassValues: List<String>,
) {
    companion object {
        private val adapter = FileService.adapter<Shapefile>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, shp: Shapefile) = FileService.toFile(file, shp, adapter)
    }
}