package org.blueventures.gemdroid.model.roi

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxExcludedRegions
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxNameCharLength
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.roiUUID
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Poly
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.common.polygons.Polygon
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.theme.MildRed
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
    var roiDrawer = PolygonDrawer(maxArea = maxRoiArea, background = ::background)

    fun refreshRois(filesDir: File, callback: (Result<List<File>>) -> Unit) = scoped { repo.getRois(filesDir).collect(callback) }

    fun saveRoi(filesDir: File, callback: (Result<Unit>) -> Unit) = scoped {
        repo.saveRoi(filesDir, roiFromState()).collect(callback)
    }

    private fun roiFromState() = ROI.fromState(
        roiName,
        contemporaryYearStart,
        contemporaryYearEnd,
        contemporaryMonthStart,
        contemporaryMonthEnd,
        historicalYearStart,
        historicalYearEnd,
        historicalMonthStart,
        historicalMonthEnd,
        roiDrawer.points(),
        polygons,
        regionUUID = roiUUID()
    )

    fun deleteRoi(dir: File, callback: (Result<Unit>) -> Unit) = scoped { repo.deleteRoi(dir).collect(callback) }
    fun getROI(roiDir: File, callback: (Result<ROI>) -> Unit) = loadFile(RoiDatasource.roiFile(roiDir), ROI.Companion, callback)

    fun importROI(prefix: String, roi: ROI) {
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
        polygons.addAll(roi.excludedRegions.map { PolygonDrawer.NamedPolygon("", it) } ?: emptyList())

        val state = GeojsonPolygon.toState(roi.polygon)
        val points = if (state.isNotEmpty()) {
            state.first().toMutableList()
        } else {
            mutableListOf()
        }
        roiDrawer = PolygonDrawer(points = points, maxArea = maxRoiArea, background = ::background)
    }

    fun notTooLong() = roiName.length <= maxNameCharLength

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

    fun clear() { clearName(); clearContemporaryYears(); clearContemporaryMonths(); clearHistoricalYears(); clearHistoricalMonths(); roiDrawer.clear(); polygons.clear(); }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    var filesDir: File = File("")
    override val named = false
    override val appBarTitleId = R.string.create_coarse_roi
    override fun appBarTitle(title: String) = title
    override var visualizer: Visualize.Visualizer? = null
    override val drawer = PolygonDrawer(background = ::background)

    override fun polygonDrawn() {
        polygons.add(PolygonDrawer.NamedPolygon(polygonName, GeojsonPolygon.fromState(listOf(drawer.points()))))
        drawer.clear()
        polygonName = ""
    }

    override fun center() = Bounds.centerFromList(roiDrawer.points())
    override val storage = Maps.Storage.fromViewModel(this)
    override fun validatePolygonName(): Boolean {
        for (region in polygons) {
            if (region.name == polygonName) {
                return false
            }
        }

        return polygonName.length <= maxNameLength && Regexp.subRegionName.matches(polygonName)
    }

    override var polygonName = ""
    override val polygonType = R.string.excluded_region
    override val maxNameLength = maxNameCharLength
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
    override fun displayRegions(callback: (List<Poly.PolygonGroup>) -> Unit): Job {
        return background({
            val polys = mutableListOf<Poly.NamedPoly>()
            for (poly in polygons) polys.add(Poly.NamedPoly(poly.name, GeojsonPolygon.toState(poly.polygon)))
            listOf(Poly.PolygonGroup(polygonTypePlural, polys, MildRed.toArgb()), backgroundPolygon())
        }, callback)
    }
    override fun backgroundPolygon(callback: (Poly.PolygonGroup?) -> Unit) = background({ backgroundPolygon() }, callback)

    private fun backgroundPolygon() = Poly.PolygonGroup(R.string.coarse_boundary, listOf(Poly.NamedPoly(roiName, listOf(roiDrawer.points()))), startChecked = false)

    override var shapefile: List<List<LatLng>> = emptyList()
    override fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = scoped { repo.validateShapefile(filesDir, streams.streams, streams.names).collect(callback) }
    override fun shapefileLooksGood() {
        polygons.add(PolygonDrawer.NamedPolygon(polygonName, GeojsonPolygon.fromState(shapefile)))
        shapefile = emptyList()
        polygonName = ""
    }

    companion object {
        const val maxRoiArea = 5_000_000 // hectares
        const val maxYearGap = 5
        const val defaultContemporaryYearStart = 2019
        const val defaultContemporaryYearEnd = 2021
        const val defaultHistoricalYearStart = 1998
        const val defaultHistoricalYearEnd = 2001
        const val defaultMonthStart = 6
        const val defaultMonthEnd = 8
        const val oldestLandsatYear = 1973
    }

    val coarseModel: Polygon.Model = CoarsePolygonModel(this)
}

class CoarsePolygonModel(private val viewModel: RoiViewModel): Polygon.Model {
    override val appBarTitleId = R.string.create_coarse_roi
    override fun appBarTitle(title: String) = title

    override val polygonType: Int = R.string.coarse_boundary
    override var visualizer: Visualize.Visualizer? = null
    override val storage: Maps.Storage = viewModel.storage
    override val drawer: PolygonDrawer
        get() = viewModel.roiDrawer

    override fun polygonDrawn() {}

    override fun center() = viewModel.center()

    override fun backgroundPolygon(callback: (Poly.PolygonGroup?) -> Unit) = viewModel.background({ null }, callback)

    override var shapefile: List<List<LatLng>> = emptyList()

    override fun <T> background(work: () -> T, callback: (T) -> Unit) = viewModel.background(work, callback)

    override fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit) = viewModel.validateShapefile(streams, callback)

    override var polygonName: String = viewModel.roiName

    override fun shapefileLooksGood() {
        val points = if (shapefile.isNotEmpty()) {
            shapefile.first().toMutableList()
        } else {
            mutableListOf()
        }

        viewModel.roiDrawer = PolygonDrawer(points = points, maxArea = RoiViewModel.maxRoiArea, background = ::background)
    }
}