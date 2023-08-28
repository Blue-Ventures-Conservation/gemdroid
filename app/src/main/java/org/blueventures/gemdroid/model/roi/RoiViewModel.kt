package org.blueventures.gemdroid.model.roi

import com.github.zibnix.droidbones.mvvm.BaseViewModel
import org.blueventures.gemdroid.data.DrawPolygon
import org.blueventures.gemdroid.data.Regexp
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
    var drawPoly = DrawPolygon(maxROIArea) { point, adder, callback ->
        scoped { repo.addPoint(point, adder).collect(callback) }
    }

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
            drawPoly.points
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

    fun notSpecial() = Regexp.roiName.matches(name)

    fun clearName() { name = "" }
    fun validateContemporaryYearsOrder() = validateDateIntsOrder(contemporaryYearStart, contemporaryYearEnd)
    fun validateContemporaryYearsGap() = validateYearGap(contemporaryYearStart, contemporaryYearEnd)
    fun clearContemporaryYears() { contemporaryYearStart = defaultContemporaryYearStart; contemporaryYearEnd = defaultContemporaryYearEnd }
    fun clearContemporaryMonths() { contemporaryMonthStart = defaultMonthStart; contemporaryMonthEnd = defaultMonthEnd}
    fun validateHistoricalYearsOrder() = validateDateIntsOrder(historicalYearStart, historicalYearEnd)
    fun validateHistoricalYearsGap() = validateYearGap(historicalYearStart, historicalYearEnd)
    fun clearHistoricalYears() { historicalYearStart = defaultHistoricalYearStart; historicalYearEnd = defaultHistoricalYearEnd}
    fun clearHistoricalMonths() { historicalMonthStart = defaultMonthStart; historicalMonthEnd = defaultMonthEnd}
    fun currentYear() = Calendar.getInstance().get(Calendar.YEAR)

    fun clear() { clearName(); clearContemporaryYears(); clearContemporaryMonths(); clearHistoricalYears(); clearHistoricalMonths(); drawPoly.clearPoints() }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    companion object {
        const val maxROIArea = 40_000 // square kilometers
        const val maxYearGap = 5
        const val defaultContemporaryYearStart = 2019
        const val defaultContemporaryYearEnd = 2021
        const val defaultHistoricalYearStart = 1998
        const val defaultHistoricalYearEnd = 2001
        const val defaultMonthStart = 6
        const val defaultMonthEnd = 8
        const val oldestLandsatYear = 1973
    }
}