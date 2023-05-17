package org.blueventures.gemdroid.data

import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import kotlin.math.abs

// A collection of functions related to unpacking (semi-)unstructured JSON
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
        val list = m[key] as? List<*> ?: return null
        return list.checkItemsAre()
    }

    fun boxChartBandInfo(m: Map<String, Any>, band: String): Pair<String, Map<String, List<Double>>>? {
        val bmap = (m[band] as? Map<*, *>)?.checkItemsAre<String, Any>() ?: return null
        val bcopy = HashMap(bmap)
        val seps = (bcopy.remove("separability") as? List<*>)?.checkItemsAre<List<String>>() ?: return null

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

        val cmap = (bcopy as? Map<*, *>)?.checkItemsAre<String, List<Double>>() ?: return null

        return Pair(sepStr, cmap)
    }

    data class PointD(val x: Double, val y: Double)

    fun scatterChartInfo(m: Map<String, Any>, classes: List<String>, bandX: String, bandY: String): Map<String, List<PointD>>? {
        val out = mutableMapOf<String, MutableList<PointD>>()

        for (cls in classes) {
            val cmap = (m[cls] as? List<*>)?.checkItemsAre<Map<String, Double>>() ?: return null
            out[cls] = mutableListOf()
            for (pt in cmap) {
                val x = pt[bandX] ?: return null
                val y = pt[bandY] ?: return null
                out[cls]!!.add(PointD(x, y))
            }
        }

        return out
    }

    enum class Correlation {
        NONE,
        MODERATE,
        HIGH;

        companion object {
            fun get(value: Double, mod: Double, high: Double) =
                when {
                    abs(value) >= high -> HIGH
                    abs(value) >= mod -> MODERATE
                    else -> NONE
                }
        }
    }

    data class CorrelationValue(val correlation: Correlation, val value: Double)

    data class Correlations(val bands: List<String>, val highThreshold: Double, val moderateThreshold: Double, val data: Map<String, List<Double>>) {
        fun row(band: String): List<CorrelationValue> {
            val vals = data[band] ?: emptyList()

            val corrs = mutableListOf<CorrelationValue>()
            for (v in vals) {
                corrs.add(CorrelationValue(Correlation.get(v, moderateThreshold, highThreshold), v))
            }

            return corrs
        }
    }

    fun correlationChartInfo(m: Map<String, Any>): Correlations? {
        val cpy = HashMap(m)
        return Correlations(
            (cpy.remove("bands") as? List<*>)?.checkItemsAre() ?: return null,
            cpy.remove("highly_correlated") as? Double ?: return null,
            cpy.remove("moderately_correlated") as? Double ?: return null,
            (cpy as? Map<*, *>)?.checkItemsAre() ?: return null
        )
    }

    private val adapter = adapter()
    fun fromFile(file: File) = FileService.fromFile(file, adapter)
    fun toFile(file: File, m: Map<String, Any>) = FileService.toFile(file, m, adapter)

    private fun adapter(): JsonAdapter<Map<String, Any>> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(type)
    }
}