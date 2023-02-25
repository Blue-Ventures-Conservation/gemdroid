package org.blueventures.gemdroid.model.roi

import android.location.Location
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.Calendar
import java.util.Collections

class RoiViewModel(private val repo: RoiRepository = RoiRepository()): BaseViewModel() {
    private val _state = MutableStateFlow(RoiState())
    val state: StateFlow<RoiState> = _state

    fun refreshRois(filesDir: File) = scoped {
        repo.getRois(filesDir).collect { files ->
            newState(RoiState(rois = files))
        }
    }

    fun saveRoi(filesDir: File, callback: (Boolean) -> Unit) = scoped {
        repo.saveRoi(filesDir, _state.value).collect {
            callback(it)
        }
    }

    fun deleteRoi(dir: File, callback: (Boolean) -> Unit) = scoped {
        repo.deleteRoi(dir).collect {
            newState(_state.value.copy(rois = null))
            callback(it)
        }
    }

    fun isUnique(name: String): Boolean {
        _state.value.rois?.let { dirs ->
            for (dir in dirs) {
                if (dir.name == name) {
                    return@isUnique false
                }
            }
        }

        return true
    }

    private val nameRegex = Regex("[a-zA-Z\\d]+[a-zA-Z\\d\\s]*")
    fun notSpecial(name: String): Boolean {
        return nameRegex.matches(name)
    }

    fun setName(name: String) = newState(_state.value.copy(name = name))
    fun setContemporaryYearStart(year: Int) = newState(_state.value.copy(contemporaryYearStart = year))
    fun setContemporaryYearEnd(year: Int) = newState(_state.value.copy(contemporaryYearEnd = year))
    fun validateContemporaryYearsOrder() = validateDateIntsOrder(_state.value.contemporaryYearStart, _state.value.contemporaryYearEnd)
    fun validateContemporaryYearsGap() = validateYearGap(_state.value.contemporaryYearStart, _state.value.contemporaryYearEnd)
    fun clearContemporaryYears() = newState(_state.value.copy(contemporaryYearStart = defaultContemporaryYearStart, contemporaryYearEnd = defaultContemporaryYearEnd))
    fun setHistoricalYearStart(year: Int) = newState(_state.value.copy(historicalYearStart = year))
    fun setHistoricalYearEnd(year: Int) = newState(_state.value.copy(historicalYearEnd = year))
    fun validateHistoricalYearsOrder() = validateDateIntsOrder(_state.value.historicalYearStart, _state.value.historicalYearEnd)
    fun validateHistoricalYearsGap() = validateYearGap(_state.value.historicalYearStart, _state.value.historicalYearEnd)
    fun clearHistoricalYears() = newState(_state.value.copy(historicalYearStart = defaultHistoricalYearStart, historicalYearEnd = defaultHistoricalYearEnd))
    fun setMonthStart(month: Int) = newState(_state.value.copy(monthStart = month))
    fun setMonthEnd(month: Int) = newState(_state.value.copy(monthEnd = month))
    fun validateMonthsOrder() = validateDateIntsOrder(_state.value.monthStart, _state.value.monthEnd)
    fun clearMonths() = newState(_state.value.copy(monthStart = defaultMonthStart, monthEnd = defaultMonthEnd))
    fun setIndices(i: Indices) = newState(_state.value.copy(indices = i))
    fun getIndices() = listOf(Indices.LS_BEST, Indices.LS_STANDARD, Indices.LS)
    fun clearIndices() = newState(_state.value.copy(indices = defaultIndices))
    fun addPoint(point: LatLng) = adjustPolygonWithRespectTo(point)
    fun clearPoints() = newState(_state.value.copy(points = arrayListOf()))
    fun polygonArea() = SphericalUtil.computeArea(_state.value.points)/1_000_000
    fun validatePolygon(): Boolean {
        val area = polygonArea()
        return area > 0 && area <= maxROIArea
    }
    fun polygonOpts(): PolygonOptions? {
        if (_state.value.points.size < 3) {
            return null
        }
        val opts = PolygonOptions().strokeWidth(2F).fillColor(0x7F00FF00)
        for (latlng in _state.value.points) {
            opts.add(latlng)
        }
        return opts
    }
    fun currentYear() = Calendar.getInstance().get(Calendar.YEAR)

    fun clear() = newState(RoiState())
    private fun newState(state: RoiState) { _state.value = state }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    private fun adjustPolygonWithRespectTo(point: LatLng) {
        val points = _state.value.points

        if (points.size > 2) {
            var minDistance = 0F
            val distances = arrayListOf<Float>()

            for (i in 0 until points.size) {
                // 1. Find the mid points of the edges of polygon
                val list: ArrayList<LatLng> = ArrayList()
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
                newState(_state.value.copy(points = rotate(points, shiftByNumber)))
            }
        }

        // 5. Now add coordinated to be drawn
        _state.value.points.add(point)
    }

    private fun minIndex(list: ArrayList<Float>): Int {
        return list.indexOf(Collections.min(list))
    }

    private fun <T> rotate(aL: ArrayList<T>, shift: Int): ArrayList<T> {
        if (aL.size == 0) return aL
        var element: T?
        for (i in 0 until shift) {
            // remove last element, add it to front of the ArrayList
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

data class RoiState(
    val rois: List<File>? = null, // nullable so we can detect when no files yet exist bt we have checked
    val name: String = "",
    val contemporaryYearStart: Int = RoiViewModel.defaultContemporaryYearStart,
    val contemporaryYearEnd: Int = RoiViewModel.defaultContemporaryYearEnd,
    val historicalYearStart: Int = RoiViewModel.defaultHistoricalYearStart,
    val historicalYearEnd: Int = RoiViewModel.defaultHistoricalYearEnd,
    val monthStart: Int = RoiViewModel.defaultMonthStart,
    val monthEnd: Int = RoiViewModel.defaultMonthEnd,
    val indices: Indices = RoiViewModel.defaultIndices,
    val points: ArrayList<LatLng> = arrayListOf(),
)

/**
 * 1: the six Landsat bands on their own = ‘LS’
 * 2: the six Landsat bands + the “best” index (CMRI) = ‘LS+best’
 * 3: the six Landsat bands + three optimal indices MNDWI, MMRI, SAVI) = ‘LS+standard’
 */
enum class Indices {
    LS {
        override fun toLabel() = "Six Landsat Bands"
        override fun toList() = emptyList<String>()
    },

    LS_BEST {
        override fun toLabel() = "Six Landsat Bands + the best index (CMRI)"
        override fun toList() = listOf("CMRI")
    },

    LS_STANDARD {
        override fun toLabel() = "Six Landsat Bands + 3 optimal indices (MNDWI, MMRI, SAVI)"
        override fun toList() = listOf("MNDWI", "MMRI", "SAVI")
    };

    abstract fun toLabel(): String
    abstract fun toList(): List<String>
}
