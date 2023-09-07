package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.DrawPolygon.Companion.squareKmInMeters
import org.blueventures.gemdroid.data.DrawPolygon.Companion.squareKms
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsStats
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav

object Details {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, downloads: Click, back: Click) {
        Nav.Wrap(back) {
            appBar(AppBarUpdate(viewModel.roi.appBar(stringResource(R.string.dynamics))))
            Col.MidPad(arrange = Arrangement.Top, scroll = true) {
                Info.Block {
                    Info.Header(viewModel.roi.name)
                    StatsBlock(viewModel.targetClass, viewModel.urls.stats)
                }
                Info.Block {
                    if (viewModel.urls.subRegionStats.isNotEmpty()) {
                        Info.Header(stringResource(R.string.sub_regions))
                        viewModel.urls.subRegionStats.forEach { stats ->
                            Info.SubHeader(stats.name ?: stringResource(R.string.unnamed))
                            StatsBlock(viewModel.targetClass, stats)
                        }
                    }
                }
                Downloads(downloads)
            }
        }
    }

    @Composable
    fun StatsBlock(targetClass: String, stats: DynamicsStats) {
        StatsRow(TargetLabel(R.string.total_cont_area, targetClass), stats.cont_area)
        StatsRow(TargetLabel(R.string.total_hist_area, targetClass), stats.hist_area)
        StatsRow(TargetLabel(R.string.loss_fmt, targetClass), stats.loss)
        StatsRow(TargetLabel(R.string.gain_fmt, targetClass), stats.gain)
        StatsRow(TargetLabel(R.string.persistence_fmt, targetClass), stats.persistence)
    }

    @Composable
    fun StatsRow(label: String, area: Double) {
        DetailsRow(label, squareKms(toSquareKm(area)))
    }

    @Composable
    fun DetailsRow(label: String, value: String) {
        Info.Row {
            Info.Txt(label)
            Info.Txt(value)
        }
    }

    @Composable
    private fun Downloads(downloads: Click) {
        Info.Block {
            Info.BlueLine()
            Info.Space()
            Col.DashboardButton(stringResource(R.string.downloads), downloads)
        }
    }

    @Composable
    private fun TargetLabel(@StringRes fmt: Int, targetClass: String) = stringResource(fmt).format(targetClass)

    private fun toSquareKm(meters: Double) = (meters/squareKmInMeters).toInt()
}