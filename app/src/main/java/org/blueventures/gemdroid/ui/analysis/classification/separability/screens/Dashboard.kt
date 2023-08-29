package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Nav

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, separation: Click, scatter: Click, correlation: Click, back: Click) {
        Nav.Wrap(back) { nav ->
            appBar(AppBarUpdate(title = viewModel.title))

            GetRemote.Save(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile, CRA::errHandler) { json ->
                Layout(viewModel, json, {
                    nav.next()
                    separation()
                }, {
                    nav.next()
                    scatter()
                }, {
                    nav.next()
                    correlation()
                }, nav::back)
            }
        }
    }

    @Composable
    fun Layout(viewModel: SeparabilityViewModel, json: Map<String, Any>, separation: Click, scatter: Click, correlation: Click, back: Click) {
        val msg = JSONMap.separabilityMessage(LocalContext.current, viewModel.timePeriod, json)

        if (msg == null) {
            Effect.Once { back() }
            return
        }

        Col.Dash(msg) {
            DashboardButton(label = stringResource(R.string.band_separation), separation)
            DashboardButton(label = stringResource(R.string.band_scatter_plot), scatter)
            DashboardButton(label = stringResource(R.string.band_correlation), correlation)
        }
    }
}