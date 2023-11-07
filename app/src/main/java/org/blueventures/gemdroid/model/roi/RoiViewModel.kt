package org.blueventures.gemdroid.model.roi

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxExcludedRegions
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import java.io.File
import java.util.Calendar

class RoiViewModel(
    private val repo: RoiRepository = RoiRepository()
): ApiViewModel(repo), Polygons.Model {
    var rois: List<File> = emptyList()
    var roiName: String = ""
    var contemporaryYearStart: Int = defaultContemporaryYearStart
    var contemporaryYearEnd: Int = defaultContemporaryYearEnd
    var contemporaryMonthStart: Int = defaultMonthStart
    var contemporaryMonthEnd: Int = defaultMonthEnd
    var historicalYearStart: Int = defaultHistoricalYearStart
    var historicalYearEnd: Int = defaultHistoricalYearEnd
    var historicalMonthStart: Int = defaultMonthStart
    var historicalMonthEnd: Int = defaultMonthEnd
    var roiDrawer = PolygonDrawer(maxArea = maxRoiArea, adder = this::polyAdder)

    private fun polyAdder(point: LatLng, adder: (LatLng) -> Unit, callback: (Unit) -> Unit) = scoped { repo.addPoint(point, adder).collect(callback) }

    fun refreshRois(filesDir: File, callback: (Result<List<File>>) -> Unit) = scoped { repo.getRois(filesDir).collect(callback) }

    fun saveRoi(filesDir: File, callback: (Result<Unit>) -> Unit) = scoped {
        repo.saveRoi(
            filesDir,
            roiName,
            contemporaryYearStart,
            contemporaryYearEnd,
            contemporaryMonthStart,
            contemporaryMonthEnd,
            historicalYearStart,
            historicalYearEnd,
            historicalMonthStart,
            historicalMonthEnd,
            roiDrawer.points,
            polygons
        ).collect(callback)
    }

    fun deleteRoi(dir: File, callback: (Result<Unit>) -> Unit) = scoped { repo.deleteRoi(dir).collect(callback) }
    fun getROI(roiDir: File, callback: (Result<ROI>) -> Unit) = loadFile(RoiDatasource.roiFile(roiDir), ROI.Companion, callback)

    fun importROI(prefix: String, roi: ROI, callback: () -> Unit) {
        roiName = prefix + roi.name
        contemporaryYearStart = roi.contYearStart
        contemporaryYearEnd = roi.contYearEnd
        contemporaryMonthStart = roi.contMonthStart
        contemporaryMonthEnd = roi.contMonthEnd
        historicalYearStart = roi.histYearStart
        historicalYearEnd = roi.histYearEnd
        historicalMonthStart = roi.histMonthStart
        historicalMonthEnd = roi.histMonthEnd
        polygons.clear()
        polygons.addAll(roi.excludedRegions?.map { PolygonDrawer.NamedPolygon("", it) } ?: emptyList())

        background({
            if (roi.polygon.coordinates.isNotEmpty()) {
                GeojsonPolygon.toState(roi.polygon).first().toMutableList()
            } else {
                mutableListOf()
            }
        }) {
            roiDrawer = PolygonDrawer(it, maxArea = maxRoiArea, adder = this::polyAdder)
            callback()
        }
    }

    fun isUnique(): Boolean {
        for (dir in rois) {
            if (dir.name == roiName) {
                return false
            }
        }

        return true
    }

    fun notSpecial() = Regexp.roiName.matches(roiName)

    fun clearName() { roiName = "" }
    fun validateContemporaryYearsOrder() = validateDateIntsOrder(contemporaryYearStart, contemporaryYearEnd)
    fun validateContemporaryYearsGap() = validateYearGap(contemporaryYearStart, contemporaryYearEnd)
    fun clearContemporaryYears() { contemporaryYearStart = defaultContemporaryYearStart; contemporaryYearEnd = defaultContemporaryYearEnd }
    fun clearContemporaryMonths() { contemporaryMonthStart = defaultMonthStart; contemporaryMonthEnd = defaultMonthEnd}
    fun validateHistoricalYearsOrder() = validateDateIntsOrder(historicalYearStart, historicalYearEnd)
    fun validateHistoricalYearsGap() = validateYearGap(historicalYearStart, historicalYearEnd)
    fun clearHistoricalYears() { historicalYearStart = defaultHistoricalYearStart; historicalYearEnd = defaultHistoricalYearEnd}
    fun clearHistoricalMonths() { historicalMonthStart = defaultMonthStart; historicalMonthEnd = defaultMonthEnd}
    fun currentYear() = Calendar.getInstance().get(Calendar.YEAR)

    fun clear() { clearName(); clearContemporaryYears(); clearContemporaryMonths(); clearHistoricalYears(); clearHistoricalMonths(); roiDrawer.points.clear() }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    var filesDir: File = File("")
    override val named = false
    override val title = R.string.create_coarse_roi
    override fun appBarTitle(title: String) = title
    override var visualizer: Visualize.Visualizer? = null
    override val drawer = PolygonDrawer(adder = this::polyAdder)

    override fun polygonDrawn() {
        polygons.add(PolygonDrawer.NamedPolygon(name, GeojsonPolygon.fromState(listOf(drawer.points))))
        drawer.points.clear()
        name = ""
    }

    override fun bounds() = roiDrawer.points

    override fun validatePolygonName(): Boolean {
        for (region in polygons) {
            if (region.name == name) {
                return false
            }
        }

        return Regexp.subRegionName.matches(name)
    }

    override var name = ""
    override val polygonType = R.string.excluded_region
    override val polygonTypePlural = R.string.excluded_regions
    override val maxPolygons = maxExcludedRegions
    override val polygons = mutableListOf<PolygonDrawer.NamedPolygon>()

    override fun loadDrawnPolygonsFile(callback: (Result<DrawnPolygonsFile>) -> Unit): Job {
        callback(Result.failure(Exception()))
        return Job()
    }
    override val optionsInit: @Composable (SnackFun, Click, @Composable () -> Unit) -> Unit = { _, _, content ->
        content()
    }
    override fun displayRegions(callback: (List<List<List<LatLng>>>) -> Unit) {
        background({
            val polys = mutableListOf<List<List<LatLng>>>()
            for (poly in polygons) {
                polys.add(GeojsonPolygon.toState(poly.polygon))
            }
            polys
        }, callback)
    }

    override var shapefile: List<List<LatLng>> = emptyList()
    override fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = scoped { repo.validateShapefile(filesDir, streams.streams, streams.names).collect(callback) }
    override fun shapefileLooksGood() {
        polygons.add(PolygonDrawer.NamedPolygon(name, GeojsonPolygon.fromState(shapefile)))
        shapefile = emptyList()
        name = ""
    }

    companion object {
        const val maxRoiArea = 40_000 // square kilometers
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