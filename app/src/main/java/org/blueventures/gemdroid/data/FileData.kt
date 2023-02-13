package org.blueventures.gemdroid.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object FileData {
    inline fun <reified T> fromFile(file: File, adapter: JsonAdapter<T> = adapter()): T? {
        return try {
            val json = file.bufferedReader().use { it.readText() }
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> toFile(file: File, t: T?, adapter: JsonAdapter<T> = adapter()): Boolean {
        return try {
            file.writeText(adapter.toJson(t))
            true
        } catch (e: Exception) {
            false
        }
    }

    inline fun <reified T> adapter(): JsonAdapter<T> {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(T::class.java)
    }
}