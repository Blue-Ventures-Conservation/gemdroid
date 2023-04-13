package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object JSONMap {
    fun bandsAndClasses(m: Map<String, Any>): Pair<List<String>, List<String>>? {
        val b = bands(m)
        val c = classes(m)
        if (b == null || c == null) return null
        return Pair(b, c)
    }

    fun bands(m: Map<String, Any>): List<String>? {
        return strList(m, "bands")
    }

    fun classes(m: Map<String, Any>): List<String>? {
        return strList(m, "classes")
    }

    private fun strList(m: Map<String, Any>, key: String): List<String>? {
        return m[key] as? List<String>
    }

    fun boxChartBandInfo(name: String, m: Map<String, Any>): Pair<String, Map<String, List<Double>>>? {
        val bmap = m[name] as? Map<String, Any> ?: return null
        val bcopy = HashMap(bmap)
        val seps = bcopy.remove("separability") as? List<List<String>> ?: return null

        val sepStr = if (seps.isEmpty()) {
            "This band does not show separability between any of your classes."
        } else {
            val builder = StringBuilder("This band provides separability between ")
            var first = true
            for (sep in seps) {
                if (sep.size < 2) {
                    continue
                }

                if (!first) {
                    builder.append(" and between ")
                }
                first = false

                builder.append("${sep[0]} & ${sep[1]}")
            }
            builder.append(".")
            builder.toString()
        }

        val cmap = bcopy as? Map<String, List<Double>> ?: return null

        return Pair(sepStr, cmap)
    }

    private val adapter = adapter()
    fun fromFile(file: File) = FileService.fromFile(file, adapter)
    fun toFile(file: File, m: Map<String, Any>) = FileService.toFile(file, m, adapter)

    private fun adapter(): JsonAdapter<Map<String, Any>> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(type)
    }
}