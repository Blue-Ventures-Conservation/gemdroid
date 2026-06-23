package org.blueventures.gemdroid.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object JSONMap : Serializer<Map<String, Any>>() {
    private val adapter = adapter()
    override fun fromFile(file: File) = fromFile(adapter, file)
    override fun toFile(file: File, data: Map<String, Any>) = toFile(adapter, file, data)

    private fun adapter(): JsonAdapter<Map<String, Any>> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(type)
    }
}
