package org.blueventures.gemdroid.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

data class CRA(
    @field:Json(name = "spatio_temporal_invariant") val spatioTemporalInvariant: Boolean,
    @field:Json(name = "use_cont_spec") val useContSpec: Boolean,
    @field:Json(name = "cont_storage_key") val contStorageKey: String,
    @field:Json(name = "hist_storage_key") val histStorageKey: Boolean,
)

object CRABuilder {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(CRA::class.java)

    fun fromFile(file: File): CRA? {
        return try {
            val json = file.bufferedReader().use { it.readText() }
            CRABuilder.adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun toFile(file: File, cra: CRA): Boolean {
        return try {
            file.writeText(CRABuilder.adapter.toJson(cra))
            true
        } catch (e: Exception) {
            false
        }
    }
}