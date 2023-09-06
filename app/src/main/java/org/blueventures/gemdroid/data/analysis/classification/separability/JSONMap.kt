package org.blueventures.gemdroid.data.analysis.classification.separability

import android.content.Context
import com.github.zibnix.droidbones.mvvm.FileService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PointD
import org.blueventures.gemdroid.data.checkItemsAre
import org.blueventures.gemdroid.model.analysis.classification.separability.TimePeriod
import java.io.File
import kotlin.math.abs

// A collection of functions related to unpacking (semi-)unstructured JSON
object JSONMap {
    fun bandsAndClasses(m: Map<String, Any>): Pair<List<String>, List<String>>? {
        val b = bands(m) ?: return null
        val c = classes(m) ?: return null
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

    fun separabilityMessage(context: Context, tp: TimePeriod, m: Map<String, Any>): String? {
        val gaps = separabilityGaps(m) ?: return null
        val temporal = context.getString(if (tp.contemporary()) R.string.contemporary else R.string.historical)

        return if (gaps.isEmpty()) {
            context.getString(R.string.good_separation_msg).format(temporal)
        } else {
            val builder = StringBuilder(context.getString(R.string.bad_separation_msg).format(temporal))
            builder.appendLine()
            builder.appendLine()
            builder.append(context.getString(R.string.separability_lacking_between))
            andBetween(context, builder, gaps)
        }
    }

    private fun separabilityGaps(m: Map<String, Any>): List<List<String>>? {
        val classes = classes(m) ?: return null
        val allPairs = classes.pairs()
        val foundPairs = separatedClasses(m) ?: return null
        val diff = allPairs.minus(foundPairs)

        val result = mutableListOf<List<String>>()
        for (pair in diff) {
            val c1 = pair.elementAt(0)
            val c2 = pair.elementAt(1)
            if (classes.indexOf(c1) < classes.indexOf(c2)) {
                result.add(listOf(c1, c2))
            } else {
                result.add(listOf(c2, c1))
            }
        }
        return result
    }

    private fun separatedClasses(m: Map<String, Any>): Set<Set<String>>? {
        val result = mutableSetOf<Set<String>>()

        val bands = bands(m) ?: return null
        for (band in bands) {
            val bmap = (m[band] as? Map<*, *>)?.checkItemsAre<String, Any>() ?: return null
            val seps = (bmap["separability"] as? List<*>)?.checkItemsAre<List<String>>() ?: return null

            for (sep in seps) {
                if (sep.size < 2) {
                    continue
                }

                result.add(setOf(sep[0], sep[1]))
            }
        }

        return result
    }

    private fun <T> Set<T>.minus(o: Set<T>): List<T> {
        val result = mutableListOf<T>()
        for (item in this) {
            if (!o.contains(item)) {
                result.add(item)
            }
        }
        return result
    }

    private fun <T> List<T>.pairs(): Set<Set<T>> {
        val result = mutableSetOf<Set<T>>()
        for (i in this.indices) {
            val item = this[i]
            for (j in i+1 until this.size) {
                result.add(setOf(item, this[j]))
            }
        }
        return result
    }

    fun boxChartBandInfo(context: Context, m: Map<String, Any>, band: String): Pair<String, Map<String, List<Double>>>? {
        val bmap = (m[band] as? Map<*, *>)?.checkItemsAre<String, Any>() ?: return null
        val bcopy = HashMap(bmap)
        val seps = (bcopy.remove("separability") as? List<*>)?.checkItemsAre<List<String>>() ?: return null

        val sepStr = if (seps.isEmpty()) {
            context.getString(R.string.band_not_show_separability)
        } else {
            val builder = StringBuilder(context.getString(R.string.band_separability_between))
            andBetween(context, builder, seps)
        }

        val cmap = (bcopy as? Map<*, *>)?.checkItemsAre<String, List<Double>>() ?: return null

        return Pair(sepStr, cmap)
    }

    private fun andBetween(context: Context, builder: StringBuilder, separations: List<List<String>>): String {
        val ab = context.getString(R.string.and_between)

        var first = true
        for (sep in separations) {
            if (sep.size < 2) {
                continue
            }

            builder.append(" ")

            if (!first) {
                builder.append(ab)
                builder.append(" ")
            }
            first = false

            builder.append("${sep[0]} & ${sep[1]}")
        }
        builder.append(".")
        return builder.toString()
    }

    data class ScatterPoint(val id: Double?, val x: Double, val y: Double)

    fun scatterChartInfo(m: Map<String, Any>, classes: List<String>, bandX: String, bandY: String): Map<String, List<ScatterPoint>>? {
        val out = mutableMapOf<String, MutableList<ScatterPoint>>()

        for (cls in classes) {
            val cmap = (m[cls] as? List<*>)?.checkItemsAre<Map<String, Double>>() ?: return null
            out[cls] = mutableListOf()
            for (pt in cmap) {
                val x = pt[bandX] ?: return null
                val y = pt[bandY] ?: return null
                val id = pt["ID"]
                out[cls]!!.add(ScatterPoint(id, x, y))
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