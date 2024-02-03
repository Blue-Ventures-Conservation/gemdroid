package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.hectareInMeters
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.hectares
import org.blueventures.gemdroid.data.analysis.dynamics.ClassDynamics
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav

object Stats {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click, downloads: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
            Col.MidPad(arrange = Arrangement.Top, scroll = true) {
                Info.Block {
                    Info.Header(viewModel.analysisRegion.name)
                    ClassBlock(viewModel.analysisClass)
                }
            }
        }
    }

    @Composable
    fun ClassBlock(classDynamics: ClassDynamics) {
        val className = classDynamics.name
        StatsRow(ClassLabel(R.string.total_cont_area, className), classDynamics.contArea)
        StatsRow(ClassLabel(R.string.total_hist_area, className), classDynamics.histArea)
        StatsRow(ClassLabel(R.string.loss_fmt, className), classDynamics.loss)
        StatsRow(ClassLabel(R.string.gain_fmt, className), classDynamics.gain)
        StatsRow(ClassLabel(R.string.persistence_fmt, className), classDynamics.persistence)
    }

    @Composable
    fun StatsRow(label: String, area: Double) {
        DetailsRow(label, hectares(toHectares(area)))
    }

    @Composable
    fun DetailsRow(label: String, value: String) {
        Info.Block {
            Info.SubHeader(label, false)
            Info.Row {
                Info.Txt(stringResource(R.string.area))
                Info.Txt(value)
            }
        }
    }

    @Composable
    private fun ClassLabel(@StringRes fmt: Int, targetClass: String) = stringResource(fmt).format(targetClass)

    private fun toHectares(meters: Double) = (meters/hectareInMeters).toInt()
}