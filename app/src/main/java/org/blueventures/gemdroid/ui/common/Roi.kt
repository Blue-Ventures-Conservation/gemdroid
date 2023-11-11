package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.localized
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
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
                Effect.Once {
                    snack(roi.exceptionOrNull()!!.localized(ctx))
                    fail()
                }
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
            points = roi.polygonToState(),
            excludedCount = roi.excludedRegions?.size ?: 0,
            header = header,
            buttonLabel = buttonLabel,
            next = next,
        )
    }

    @Composable
    fun OverviewFromState(name: String, contYearStart: Int, contYearEnd: Int, contMonthStart: Int, contMonthEnd: Int, histYearStart: Int, histYearEnd: Int, histMonthStart: Int, histMonthEnd: Int, points: List<LatLng>, excludedCount: Int, header: String, buttonLabel: String, next: Click) {
        Col.Col(scroll = true) {
            Info.Block {
                Info.Header(title = header)
                Info.BlueLine()
                OverviewRow(stringResource(R.string.overview_name), name)
                OverviewRow(stringResource(R.string.overview_contemporary_years), "$contYearStart - $contYearEnd")
                OverviewRow(stringResource(R.string.overview_contemporary_months), "$contMonthStart - $contMonthEnd")
                OverviewRow(stringResource(R.string.overview_historical_years), "$histYearStart - $histYearEnd")
                OverviewRow(stringResource(R.string.overview_historical_months), "$histMonthStart - $histMonthEnd")
                OverviewRow(stringResource(R.string.overview_polygon_points), stringResource(R.string.overview_points).format(points.size.toString()))
                OverviewRow(stringResource(R.string.overview_polygon_area), PolygonDrawer.areaStr(points))
                OverviewRow(stringResource(R.string.overview_excluded_regions), stringResource(R.string.overview_regions).format("$excludedCount"))
            }
            DashboardButton(buttonLabel, next)
        }
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