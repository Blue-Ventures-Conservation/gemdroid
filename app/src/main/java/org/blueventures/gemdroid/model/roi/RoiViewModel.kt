package org.blueventures.gemdroid.model.roi

import com.github.zibnix.droidbones.mvvm.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import java.io.File
import java.util.Calendar

class RoiViewModel(
    private val repo: RoiRepository = RoiRepository()
): BaseViewModel() {
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
    fun clearContemporaryMonths() { contemporaryMonthStart = defaultMonthStart; contemporaryMonthEnd = defaultMonthEnd}
    fun validateHistoricalYearsOrder() = validateDateIntsOrder(historicalYearStart, historicalYearEnd)
    fun validateHistoricalYearsGap() = validateYearGap(historicalYearStart, historicalYearEnd)
    fun clearHistoricalYears() { historicalYearStart = defaultHistoricalYearStart; historicalYearEnd = defaultHistoricalYearEnd}
    fun clearHistoricalMonths() { historicalMonthStart = defaultMonthStart; historicalMonthEnd = defaultMonthEnd}
    fun addPoint(point: LatLng, callback: () -> Unit) = scoped { repo.addPoint(points, point).collect { poly ->
        points = poly
        callback()
    }}
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

    fun polygonSquareKms() = squareKms(polygonArea().toInt())
    fun squareKms(km: Int) = "${"%,d".format(km)} km²"

    fun clear() { clearName(); clearContemporaryYears(); clearContemporaryMonths(); clearHistoricalYears(); clearHistoricalMonths(); clearPoints() }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    companion object {
        const val maxROIArea = 40_000 // square kilometers
        const val maxYearGap = 5
        const val defaultContemporaryYearStart = 2019
        const val defaultContemporaryYearEnd = 2021
        const val defaultHistoricalYearStart = 1999
        const val defaultHistoricalYearEnd = 2001
        const val defaultMonthStart = 6
        const val defaultMonthEnd = 8
        const val oldestLandsatYear = 1973
    }
}