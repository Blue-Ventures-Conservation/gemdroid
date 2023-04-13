package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class JSONMap(private val m: MutableMap<String, Any>): MutableMap<String, Any> by m {

    fun bandsAndClasses(): Pair<List<String>, List<String>>? {
        val b = bands()
        val c = classes()
        if (b == null || c == null) return null
        return Pair(b, c)
    }

    fun bands(): List<String>? {
        return strList("bands")
    }

    fun classes(): List<String>? {
        return strList("classes")
    }

    private fun strList(key: String): List<String>? {
        return get(key) as? List<String>
    }

    fun boxChartBandInfo(name: String): Pair<String, Map<String, List<Double>>>? {
        val bmap = get(name) as? JSONMap ?: return null
        val seps = bmap.remove("separability") as? List<List<String>> ?: return null
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

        val cmap = bmap as? Map<String, List<Double>> ?: return null

        return Pair(sepStr, cmap)
    }

    companion object {
        private val adapter = adapter()
        fun fromFile(file: File) = FileService.fromFile(file, adapter)
        fun toFile(file: File, m: JSONMap) = FileService.toFile(file, m, adapter)

        private fun adapter(): JsonAdapter<JSONMap> {
            val type = Types.newParameterizedType(MutableMap::class.java, String::class.java, Any::class.java)
            return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(type)
        }
    }
}