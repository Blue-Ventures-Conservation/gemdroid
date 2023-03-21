package org.blueventures.gemdroid.model.roi

import android.location.Location
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import java.io.File
import java.util.Calendar
import java.util.Collections

class RoiViewModel(private val repo: RoiRepository = RoiRepository()): BaseViewModel() {
    var rois: List<File> = emptyList()
    var name: String = ""
    var contemporaryYearStart: Int = defaultContemporaryYearStart
    var contemporaryYearEnd: Int = defaultContemporaryYearEnd
    var contemporaryMonthStart: Int = defaultMonthStart
    var contemporaryMonthEnd: Int = defaultMonthEnd
    var historicalYearStart: Int = defaultHistoricalYearStart
    var historicalYearEnd: Int = defaultHistoricalYearEnd
    var historicalMonthStart: Int = defaultMonthStart
    var historicalMonthEnd: Int = defaultMonthEnd
    var indices: Indices = defaultIndices
    var points: MutableList<LatLng> = mutableListOf()

    fun refreshRois(filesDir: File, callback: (Result<List<File>>) -> Unit) = scoped { repo.getRois(filesDir).collect(callback) }

    fun saveRoi(filesDir: File, callback: (Result<Unit>) -> Unit) = scoped {
        repo.saveRoi(
            filesDir,
            name,
            contemporaryYearStart,
            contemporaryYearEnd,
            contemporaryMonthStart,
            contemporaryMonthEnd,
            historicalYearStart,
            historicalYearEnd,
            historicalMonthStart,
            historicalMonthEnd,
            indices.list(),
            points
        ).collect(callback)
    }

    fun deleteRoi(dir: File, callback: (Result<Unit>) -> Unit) = scoped { repo.deleteRoi(dir).collect(callback) }

    fun isUnique(): Boolean {
        for (dir in rois) {
            if (dir.name == name) {
                return false
            }
        }

        return true
    }

    private val nameRegex by lazy { Regex("[a-zA-Z\\d]+[a-zA-Z\\d\\s]*") }
    fun notSpecial() = nameRegex.matches(name)

    fun clearName() { name = "" }
    fun validateContemporaryYearsOrder() = validateDateIntsOrder(contemporaryYearStart, contemporaryYearEnd)
    fun validateContemporaryYearsGap() = validateYearGap(contemporaryYearStart, contemporaryYearEnd)
    fun clearContemporaryYears() { contemporaryYearStart = defaultContemporaryYearStart; contemporaryYearEnd = defaultContemporaryYearEnd }
    fun validateContemporaryMonthsOrder() = validateDateIntsOrder(contemporaryMonthStart, contemporaryMonthEnd)
    fun clearContemporaryMonths() { contemporaryMonthStart = defaultMonthStart; contemporaryMonthEnd = defaultMonthEnd}
    fun validateHistoricalYearsOrder() = validateDateIntsOrder(historicalYearStart, historicalYearEnd)
    fun validateHistoricalYearsGap() = validateYearGap(historicalYearStart, historicalYearEnd)
    fun clearHistoricalYears() { historicalYearStart = defaultHistoricalYearStart; historicalYearEnd = defaultHistoricalYearEnd}
    fun validateHistoricalMonthsOrder() = validateDateIntsOrder(historicalMonthStart, historicalMonthEnd)
    fun clearHistoricalMonths() { historicalMonthStart = defaultMonthStart; historicalMonthEnd = defaultMonthEnd}
    fun getIndices() = listOf(Indices.LS_BEST, Indices.LS_STANDARD, Indices.LS)
    fun clearIndices() { indices = defaultIndices }
    fun addPoint(point: LatLng) = adjustPolygonWithRespectTo(point)
    fun clearPoints() { points = mutableListOf() }
    fun polygonArea() = SphericalUtil.computeArea(points)/1_000_000
    fun validatePolygon(): Boolean {
        val area = polygonArea()
        return area > 0 && area <= maxROIArea
    }
    fun polygonOpts(): PolygonOptions? {
        if (points.size < 3) {
            return null
        }
        val opts = PolygonOptions().strokeWidth(2F).fillColor(0x7F00FF00)
        for (latlng in points) {
            opts.add(latlng)
        }
        return opts
    }
    fun currentYear() = Calendar.getInstance().get(Calendar.YEAR)

    fun clear() { clearName(); clearContemporaryYears(); clearContemporaryMonths(); clearHistoricalYears(); clearHistoricalMonths(); clearIndices(); clearPoints() }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    private fun adjustPolygonWithRespectTo(point: LatLng) {
        val points = this.points

        if (points.size > 2) {
            var minDistance = 0F
            val distances = mutableListOf<Float>()

            for (i in points.indices) {
                // 1. Find the mid points of the edges of polygon
                val list = mutableListOf<LatLng>()
                if (i == points.size - 1) {
                    list.add(points[points.size - 1])
                    list.add(points[0])
                } else {
                    list.add(points[i])
                    list.add(points[i + 1])
                }
                val midPoint = computeCentroid(list)

                // 2. Calculate the nearest coordinate by finding distance between mid point of each edge and the coordinate to be drawn
                val startPoint = Location("")
                startPoint.latitude = point.latitude
                startPoint.longitude = point.longitude
                val endPoint = Location("")
                endPoint.latitude = midPoint.latitude
                endPoint.longitude = midPoint.longitude
                val distance = startPoint.distanceTo(endPoint)
                distances.add(distance)
                if (i == 0) {
                    minDistance = distance
                } else {
                    if (distance < minDistance) {
                        minDistance = distance
                    }
                }
            }

            // 3. The nearest coordinate = the edge with minimum distance from mid point to the coordinate to be drawn
            val position = minIndex(distances)

            // 4. move the nearest coordinate at the end by shifting array right
            val shiftByNumber: Int = points.size - position - 1
            if (shiftByNumber != points.size) {
                this.points = rotate(points, shiftByNumber)
            }
        }

        // 5. Now add coordinated to be drawn
        this.points.add(point)
    }

    private fun minIndex(list: List<Float>): Int {
        return list.indexOf(Collections.min(list))
    }

    private fun <T> rotate(aL: MutableList<T>, shift: Int): MutableList<T> {
        if (aL.size == 0) return aL
        var element: T?
        for (i in 0 until shift) {
            // remove last element, add it to front of the List
            element = aL.removeAt(aL.size - 1)
            aL.add(0, element)
        }
        return aL
    }

    private fun computeCentroid(points: List<LatLng>): LatLng {
        var latitude = 0.0
        var longitude = 0.0
        val n = points.size
        for (point in points) {
            latitude += point.latitude
            longitude += point.longitude
        }
        return LatLng(latitude / n, longitude / n)
    }

    companion object {
        const val maxROIArea = 10_000 // square kilometers
        const val maxYearGap = 5
        const val defaultContemporaryYearStart = 2019
        const val defaultContemporaryYearEnd = 2021
        const val defaultHistoricalYearStart = 1999
        const val defaultHistoricalYearEnd = 2001
        const val defaultMonthStart = 6
        const val defaultMonthEnd = 8
        const val oldestLandsatYear = 1973
        val defaultIndices = Indices.LS_BEST
    }
}

/**
 * 1: the six Landsat bands on their own = ‘LS’
 * 2: the six Landsat bands + the “best” index (CMRI) = ‘LS+best’
 * 3: the six Landsat bands + three optimal indices MNDWI, MMRI, SAVI) = ‘LS+standard’
 */
enum class Indices {
    LS {
        override fun label() = "Six Landsat Bands"
        override fun list() = emptyList<String>()
    },

    LS_BEST {
        override fun label() = "Six Landsat Bands + the best index (CMRI)"
        override fun list() = listOf("CMRI")
    },

    LS_STANDARD {
        override fun label() = "Six Landsat Bands + 3 optimal indices (MNDWI, MMRI, SAVI)"
        override fun list() = listOf("MNDWI", "MMRI", "SAVI")
    };

    abstract fun label(): String
    abstract fun list(): List<String>
}
