package org.blueventures.gemdroid.model.roi

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.FileStream
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.data.Regexp
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxNameCharLength
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.roiUUID
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.forceLandsat
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygon
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygons
import org.blueventures.gemdroid.ui.theme.MildRed
import java.io.File
import java.util.Calendar

class RoiViewModel(
    private val repo: RoiRepository = RoiRepository()
): ApiViewModel(repo), CollectPolygons.Model {
    var rois: List<File> = emptyList()
    var roiName: String = ""
    var contemporaryYearStart: Int = defaultContemporaryYearStart
    var contemporaryYearEnd: Int = defaultContemporaryYearEnd
    var contemporaryMonths: List<Int> = emptyList()
    var historicalYearStart: Int = defaultHistoricalYearStart
    var historicalYearEnd: Int = defaultHistoricalYearEnd
    var historicalMonths: List<Int> = emptyList()
    var inlandMang = false
    var importedROI: MultiPolyPts = emptyList()
    var importedBufferDist: Int = -1
    var imported = false
    var forceLS: Boolean = false
    val coarseModel: CollectPolygon.Model = CoarsePolygonModel(this)

    fun refreshRois(filesDir: File, callback: (Result<List<File>>) -> Unit) = scoped { repo.getRois(filesDir).collect(callback) }

    fun saveRoi(filesDir: File, callback: (Result<Unit>) -> Unit) = scoped {
        repo.saveRoi(filesDir, roiFromState()).collect(callback)
    }

    private fun roiFromState() = ROI.fromState(
        roiName,
        historicalYearStart,
        historicalYearEnd,
        historicalMonths,
        contemporaryYearStart,
        contemporaryYearEnd,
        contemporaryMonths,
        multiPolyFromState(),
        inlandMang,
        polygons,
        buffDist = importedBufferDist,
        regionUUID = roiUUID(),
        forceLandsat = forceLS
    )

    fun multiPolyFromState() = importedROI.ifEmpty {
        listOf(listOf(coarseModel.drawnPoints))
    }

    fun deleteRoi(dir: File, callback: (Result<Unit>) -> Unit) = scoped { repo.deleteRoi(dir).collect(callback) }
    fun getROI(roiDir: File, callback: (Result<ROI>) -> Unit) = loadFile(RoiDatasource.roiFile(roiDir), ROI.Companion, callback)

    fun importROI(prefix: String, roi: ROI) {
        imported = true
        roiName = prefix + roi.name
        contemporaryYearStart = roi.contYearStart
        contemporaryYearEnd = roi.contYearEnd
        contemporaryMonths = roi.contMonths ?: emptyList()
        historicalYearStart = roi.histYearStart
        historicalYearEnd = roi.histYearEnd
        historicalMonths = roi.histMonths ?: emptyList()
        importedBufferDist = roi.buffDist
        polygons.clear()
        polygons.addAll(roi.excludedRegions.map { PolygonUtils.NamedPolygon("", it) })
        if (roi.polygon.coordinates.size > 1) {
            // there's more than one polygon, so it likely came in via shapefile
            importedROI = GeojsonMultiPolygon.toState(roi.polygon)
        } else {
            coarseModel.drawnPoints = firstRing(GeojsonMultiPolygon.toState(roi.polygon))
        }
        inlandMang = roi.inlandMang
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

    fun clearImportedROI() { importedROI = emptyList() }
    fun clearImported() { imported = false }
    fun clearName() { roiName = "" }
    fun validateHistoricalYearsOrder() = validateDateIntsOrder(historicalYearStart, historicalYearEnd)
    fun validateHistoricalYearsGap() = validateYearGap(historicalYearStart, historicalYearEnd)
    fun clearHistoricalYears() { historicalYearStart = defaultHistoricalYearStart; historicalYearEnd = defaultHistoricalYearEnd}
    fun clearHistoricalMonths() { historicalMonths = emptyList() }
    fun validateContemporaryYearsOrder() = validateDateIntsOrder(contemporaryYearStart, contemporaryYearEnd)
    fun validateContemporaryYearsGap() = validateYearGap(contemporaryYearStart, contemporaryYearEnd)
    fun clearContemporaryYears() { contemporaryYearStart = defaultContemporaryYearStart; contemporaryYearEnd = defaultContemporaryYearEnd }
    fun clearContemporaryMonths() { contemporaryMonths = emptyList() }
    fun currentYear() = thisYear()

    fun getForceLandsat(context: Context, callback: (Boolean) -> Unit) = read(context, forceLandsat.key, forceLandsat.default, callback)
    fun shouldUseS2() = RoiDatasource.shouldUseS2(historicalYearStart, historicalMonths, contemporaryYearStart, contemporaryMonths)

    // the alternative to clearing state like this is to tie the lifecycle of the viewmodel to something more temporary,
    // like a fragment or a nav graph destination. Maybe that would have been better, and yet, do I really want to have
    // that many view models? Do I want to have to think that much about how to pass state between them all? I do not.
    fun clearState() { clearName(); clearImportedROI(); clearImported(); clearContemporaryYears(); clearContemporaryMonths(); clearHistoricalYears(); clearHistoricalMonths(); drawnPoints = emptyList(); coarseModel.drawnPoints = emptyList(); polygons.clear(); }
    private fun validateDateIntsOrder(d1: Int, d2: Int) = d1 <= d2
    private fun validateYearGap(y1: Int, y2: Int) = (y2 - y1) <= maxYearGap

    var filesDir: File = File("")
    override val named = false
    override fun goBack() { importedROI = emptyList() }
    override val appBarTitleId = R.string.create_coarse_boundary
    override fun appBarTitle(title: String) = title

    // used when drawing excluded regions
    override var drawnPoints = emptyList<LatLng>()
    override fun polygonDrawn() {
        polygons.add(PolygonUtils.NamedPolygon(polygonName, GeojsonMultiPolygon.fromState(listOf(listOf(drawnPoints)))))
        drawnPoints = emptyList()
        polygonName = ""
    }

    override fun validatePolygonName(): Boolean {
        for (region in polygons) {
            if (region.name == polygonName) {
                return false
            }
        }

        return polygonName.length <= maxNameCharLength && Regexp.subRegionName.matches(polygonName)
    }

    override fun visualizer() = null
    override fun center() = coarseModel.center()

    override var polygonName = ""
    override val polygons = mutableListOf<PolygonUtils.NamedPolygon>()

    override fun edit() = false
    override fun clearEdit() {}

    override fun loadDrawnPolygonsFile(callback: (Result<DrawnPolygonsFile>) -> Unit) {
        background({ Result.failure(Throwable("pass")) }, callback)
    }

    @Composable
    override fun OptionsInit(snack: SnackFun, back: Click, content: @Composable () -> Unit) {
        content()
    }

    override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit): Job {
        val excludedRegionsTitle = context.getString(R.string.excluded_regions)
        return background({
            val polys = mutableListOf<Polygons.Named>()
            for (poly in polygons) polys.add(Polygons.Named(poly.name, GeojsonMultiPolygon.toState(poly.polygon)))
            val list = mutableListOf<Polygons.Group>()
            if (polys.isNotEmpty()) {
                list.add(Polygons.Group(excludedRegionsTitle, polys, MildRed.toArgb()))
            }
            val coarseTitle = context.getString(R.string.coarse_boundary)
            val coarsePolygon = Polygons.Group(
                coarseTitle,
                listOf(Polygons.Named(
                    roiName,
                    listOf(listOf(coarseModel.drawnPoints)))
                ))
            list.add(coarsePolygon)
            list
        }, callback)
    }

    override var filePoly: MultiPolyPts = emptyList()
    override fun validatePolygonFile(streams: FileStream.Streams, callback: (Result<MultiPolyPts>) -> Unit) = scoped { repo.validatePolygonFile(filesDir, streams.streams, streams.names).collect(callback) }
    override fun shapefileLooksGood() {
        polygons.add(PolygonUtils.NamedPolygon(polygonName, GeojsonMultiPolygon.fromState(filePoly)))
        filePoly = emptyList()
        polygonName = ""
    }

    companion object {
        fun thisYear() = Calendar.getInstance().get(Calendar.YEAR)
        val defaultContemporaryYearEnd = thisYear() - 1
        val defaultContemporaryYearStart = defaultContemporaryYearEnd
        val defaultHistoricalYearEnd = defaultContemporaryYearEnd - 5
        val defaultHistoricalYearStart = defaultHistoricalYearEnd
        const val maxRoiArea = 2_500_000 // hectares
        const val maxYearGap = 4
        const val oldestLandsatYear = 1982

        fun firstRing(multi: MultiPolyPts) =
            if (multi.isEmpty() || multi.first().isEmpty()) {
                mutableListOf()
            } else {
                multi.first().first().toMutableList()
            }
    }

    var isAssessmentEditor = false
}

class CoarsePolygonModel(private val viewModel: RoiViewModel): CollectPolygon.Model {
    override val appBarTitleId = R.string.create_coarse_boundary
    override var drawnPoints = emptyList<LatLng>()

    override fun visualizer() = null
    override fun center() = PolygonUtils.centerFromRing(drawnPoints)
    override fun appBarTitle(title: String) = title
    override fun edit() = viewModel.imported
    override fun clearEdit() = viewModel.clearImported()
    override fun polygonDrawn() {}
    override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) = viewModel.background({ emptyList() }, callback)
    override var filePoly: MultiPolyPts = emptyList()
    override fun <T> background(work: () -> T, callback: (T) -> Unit) = viewModel.background(work, callback)
    override fun validatePolygonFile(streams: FileStream.Streams, callback: (Result<MultiPolyPts>) -> Unit) = viewModel.validatePolygonFile(streams, callback)
    override var polygonName: String = viewModel.roiName

    override fun shapefileLooksGood() {
        viewModel.importedROI = filePoly
        if (filePoly.size == 1) {
            drawnPoints = RoiViewModel.firstRing(filePoly)
        }
    }
}