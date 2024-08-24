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
import org.blueventures.gemdroid.data.PolygonDrawer
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
                snack.once(roi.exceptionOrNull()!!.localized(ctx))
                fail.once()
            }
            else -> {
                setter(roi.getOrNull()!!)
            }
        }
    }

    @Composable
    fun OverviewFromROI(roi: ROI, header: String, buttonLabel: String, next: Click) {
        OverviewFromState(
            name = roi.name,
            contYearStart = roi.contYearStart,
            contYearEnd = roi.contYearEnd,
            contMonthStart = roi.contMonthStart,
            contMonthEnd = roi.contMonthEnd,
            histYearStart = roi.histYearStart,
            histYearEnd = roi.histYearEnd,
            histMonthStart = roi.histMonthStart,
            histMonthEnd = roi.histMonthEnd,
            multi = roi.boundaryPolyToState(),
            excluded = roi.excludedRegions,
            useS2 = roi.useS2(),
            header = header,
            buttonLabel = buttonLabel,
            next = next,
        )
    }

    @Composable
    fun OverviewFromState(name: String, contYearStart: Int, contYearEnd: Int, contMonthStart: Int, contMonthEnd: Int, histYearStart: Int, histYearEnd: Int, histMonthStart: Int, histMonthEnd: Int, multi: MultiPolyPts, excluded: List<GeojsonMultiPolygon>, useS2: Boolean, header: String, buttonLabel: String, next: Click) {
        Col.Col(scroll = true) {
            Info.Block {
                Info.Header(title = header)

                val nameLabel = stringResource(R.string.overview_name)
                val (overflowed, setOverflowed) = remember { mutableStateOf<Boolean?>(null) }
                when(overflowed) {
                    null -> MeasureName(nameLabel, name, setOverflowed)
                    true -> WrappedName(name)
                    false -> OverviewRow(nameLabel, name)
                }

                var excludedPolygons = 0
                var excludedArea = 0
                for (region in excluded) {
                    val excl = GeojsonMultiPolygon.toState(region)
                    excludedPolygons += excl.size
                    excludedArea += PolygonDrawer.areaHectares(excl)
                }

                OverviewRow(stringResource(R.string.overview_contemporary_years), "$contYearStart - $contYearEnd")
                OverviewRow(stringResource(R.string.overview_contemporary_months), "$contMonthStart - $contMonthEnd")
                OverviewRow(stringResource(R.string.overview_historical_years), "$histYearStart - $histYearEnd")
                OverviewRow(stringResource(R.string.overview_historical_months), "$histMonthStart - $histMonthEnd")
                OverviewRow(stringResource(R.string.overview_polygon_area), PolygonDrawer.areaStr(multi))
                OverviewRow(stringResource(R.string.overview_excluded_regions), stringResource(R.string.overview_regions).format("$excludedPolygons"))
                OverviewRow(stringResource(R.string.overview_excluded_area), PolygonDrawer.hectares(excludedArea))
                OverviewRow(stringResource(R.string.satellites), if (useS2) stringResource(R.string.sentinel_2) else stringResource(R.string.landsat))
            }
            DashboardButton(buttonLabel, next)
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
                Info.Txt(stringResource(R.string.overview_name))
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