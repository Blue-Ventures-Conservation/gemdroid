package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.SeparabilityJSON
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Once

object Dashboard {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBar, back: Click, separation: Click, scatter: Click, correlation: Click) {
        appBar.Update(AppBarUpdate(viewModel.title))

        GetRemote.Save(viewModel::loadSeparationFile, viewModel::getSeparation, viewModel::saveSeparationFile, errorHandler = CRA::errHandler) { json ->
            Layout(viewModel, json, back, {
                separation()
            }, {
                scatter()
            }) {
                correlation()
            }
        }
    }

    @Composable
    fun Layout(viewModel: SeparabilityViewModel, json: Map<String, Any>, back: Click, separation: Click, scatter: Click, correlation: Click) {
        val msg = SeparabilityJSON.separabilityMessage(LocalContext.current, viewModel.timePeriod, json)

        if (msg == null) {
            back.Once()
            return
        }

        Col.Dash(msg) {
            DashboardButton(label = stringResource(R.string.band_separation), separation)
            DashboardButton(label = stringResource(R.string.band_scatter_plot), scatter)
            DashboardButton(label = stringResource(R.string.band_correlation), correlation)
        }
    }
}