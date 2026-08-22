package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.data.PolygonUtils.hectaresString
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.data.analysis.cra.ContemporaryAndHistoricalCRAs
import org.blueventures.gemdroid.data.analysis.cra.RemoteCRAFileInfo
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.ui.common.Col.DashboardButton

object Roi {
    @Composable
    fun Loader(getter: ((Result<ROI>?) -> Unit) -> Unit, snack: SnackFun, fail: Click, setter: @Composable (ROI) -> Unit) {
        val (roi, setRoi) = remember { mutableStateOf<Result<ROI>?>(null) }

        when {
            roi == null -> {
                Progress()
                getter(setRoi)
            }
            roi.isFailure -> {
                Progress()
                val ctx = LocalContext.current
                snack.Once(roi.exceptionOrNull()!!.localized(ctx))
                fail.Once()
            }
            else -> {
                setter(roi.getOrNull()!!)
            }
        }
    }

    @Composable
    fun OverviewFromROI(background: (() -> Triple<Int, Int, Int>, (Triple<Int, Int, Int>) -> Unit) -> Unit, roi: ROI, cras: ContemporaryAndHistoricalCRAs?, header: String, buttonLabel: String, next: Click) {
        OverviewFromState(
            background,
            name = roi.name,
            histYearStart = roi.histYearStart,
            histYearEnd = roi.histYearEnd,
            histMonths = roi.histMonths ?: emptyList(),
            contYearStart = roi.contYearStart,
            contYearEnd = roi.contYearEnd,
            contMonths = roi.contMonths ?: emptyList(),
            multi = roi.boundaryPolyToState(),
            excluded = roi.excludedRegions,
            useS2 = roi.useS2(),
            cras = cras,
            header = header,
            buttonLabel = buttonLabel,
            next = next,
        )
    }

    @Composable
    fun OverviewFromState(background: (() -> Triple<Int, Int, Int>, (Triple<Int, Int, Int>) -> Unit) -> Unit, name: String, histYearStart: Int, histYearEnd: Int, histMonths: List<Int>, contYearStart: Int, contYearEnd: Int, contMonths: List<Int>, multi: MultiPolyPts, excluded: List<GeojsonMultiPolygon>, useS2: Boolean, cras: ContemporaryAndHistoricalCRAs?, header: String, buttonLabel: String, next: Click) {
        val (calcs, setCalcs) = remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }
        when (calcs) {
            null -> {
                Progress()
                background({
                    var excludedPolygons = 0
                    var excludedArea = 0
                    var outsideHectares = 0
                    for (region in excluded) {
                        val pair = GeojsonMultiPolygon.toStateWithContainer(region, multi)
                        val excl = pair.first
                        excludedPolygons += excl.size
                        excludedArea += PolygonUtils.multiPolygonAreaHectares(excl).toInt()
                        outsideHectares += pair.second
                    }

                    excludedArea -= outsideHectares
                    val polyArea = PolygonUtils.multiPolygonAreaHectares(multi).toInt()
                    Triple(polyArea, excludedPolygons, excludedArea)
                }, setCalcs)
            }
            else -> {
                Col.Col(scroll = true) {
                    Info.Block {
                        Info.Header(title = header)

                        val nameLabel = stringResource(R.string.overview_project_name)
                        val (overflowed, setOverflowed) = remember { mutableStateOf<Boolean?>(null) }
                        when(overflowed) {
                            null -> MeasureName(nameLabel, name, setOverflowed)
                            true -> WrappedName(name)
                            false -> OverviewRow(nameLabel, name)
                        }

                        OverviewRow(stringResource(R.string.overview_historical_years), "$histYearStart - $histYearEnd")
                        OverviewRow(stringResource(R.string.overview_historical_months), histMonths.joinToString(separator = ", "))
                        OverviewRow(stringResource(R.string.overview_contemporary_years), "$contYearStart - $contYearEnd")
                        OverviewRow(stringResource(R.string.overview_contemporary_months), contMonths.joinToString(separator = ", "))
                        OverviewRow(stringResource(R.string.overview_boundary_area), hectaresString(calcs.first))
                        OverviewRow(stringResource(R.string.overview_excluded_regions), stringResource(R.string.overview_regions).format("${calcs.second}"))
                        if (calcs.second > 0) {
                            OverviewRow(stringResource(R.string.overview_excluded_area), hectaresString(calcs.third))
                            OverviewRow(stringResource(R.string.overview_net_area), hectaresString(calcs.first - calcs.third))
                        }
                        OverviewRow(stringResource(R.string.satellites), if (useS2) stringResource(R.string.sentinel_2) else stringResource(R.string.landsat))
                    }
                    cras?.let {
                        if (it.historicalCRA != null) {
                                CRAOverview(stringResource(R.string.historical_cras), it.historicalCRA)
                        }
                        CRAOverview(stringResource(R.string.contemporary_cras), it.contemporaryCRA)
                    }
                    DashboardButton(buttonLabel, next)
                }
            }
        }
    }

    @Composable
    private fun CRAOverview(header: String, info: RemoteCRAFileInfo) {
        if (info.classCounts?.isNotEmpty() == true) {
            Info.Block {
                Info.Header(header)

                OverviewRow(stringResource(R.string.name), info.storageKey())

                var total = 0
                val counts = info.classCounts
                for (cc in counts) {
                    total += cc.craCount
                    val translatedBVName = BVClass.fromName(cc.className)?.stringID()?.let { stringResource(it) }
                    OverviewRow("${cc.classNumber} - ${translatedBVName ?: cc.className}:", "${cc.craCount}")
                }

                OverviewRow(stringResource(R.string.total), "$total")
            }
        }
    }

    @Composable
    private fun MeasureName(label: String, name: String, setOverflowed: (Boolean?) -> Unit) {
        Info.Row(verticalPadding = 16.dp) {
            Info.Txt(label)
            Text(text = name, fontSize = Info.txtSize, maxLines = 1, onTextLayout = { layout ->
                setOverflowed(layout.hasVisualOverflow)
            })
        }
    }

    @Composable
    private fun WrappedName(name: String) {
        Info.Row(verticalPadding = 16.dp) {
            Column {
                Info.Txt(stringResource(R.string.overview_project_name))
                Info.Txt(name)
            }
        }
        Info.BlueLine()
    }

    @Composable
    fun OverviewRow(label: String, value: String) {
        Info.Row(verticalPadding = 16.dp) {
            Info.Txt(label)
            Info.Txt(value)
        }
        Info.BlueLine()
    }
}