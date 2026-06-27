package org.blueventures.gemdroid.data.analysis.cra

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.Json
import java.io.File

data class ClassCount(
    @Json(name = "class_name") val className: String,
    @Json(name = "class_number") var classNumber: Int,
    @Json(name = "cra_count") var craCount: Int,
)

// Saved in Cloud Storage alongside the zip to keep track of
// the parsed/selected fields used for analysis
data class RemoteCRAFileInfo(
    @Json(name = "shp_storage_key") val shapefileStorageKey: String?,
    @Json(name = "json_storage_key") val jsonStorageKey: String?,
    @Json(name = "table_upload_operation_name") val tableUploadOperationName: String,
    @Json(name = "numeric_class_field") val numericClassField: String,
    @Json(name = "string_class_field") val stringClassField: String,
    @Json(name = "string_class_field_values") val stringClassValues: List<String>,
    @Json(name = "class_cra_counts") val classCounts: List<ClassCount>?
) {
    fun storageKey(): String {
        if (shapefileStorageKey != null) return shapefileStorageKey
        return jsonStorageKey!!
    }
    companion object {
        private val adapter = FileService.adapter<RemoteCRAFileInfo>()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, info: RemoteCRAFileInfo) = FileService.toFile(file, info, adapter)
    }
}