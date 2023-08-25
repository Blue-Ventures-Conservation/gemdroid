package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Dropdown
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote

object ScatterChoices {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, back: Click, next: Click) {
        appBar(AppBarUpdate(title = viewModel.title))

        GetRemote.AwaitSave(viewModel::loadScatterFile, viewModel::getScatter, viewModel::saveScatterFile, CRA::errHandler) { json ->
            val bands = JSONMap.bands(json)

            if (bands == null) {
                Effect.Once { back() }
                return@AwaitSave
            }

            Choices(viewModel, bands, next)
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun Choices(viewModel: SeparabilityViewModel, bands: List<String>, next: Click) {
        val (bandX, setBandX) = remember { mutableStateOf<String?>(null) }
        val (bandY, setBandY) = remember { mutableStateOf<String?>(null) }
        Col.Col {
            Dropdown(title = stringResource(R.string.select_x_band), labels = bands) { setBandX(bands[it]) }
            Dropdown(title = stringResource(R.string.select_y_band), labels = bands) { setBandY(bands[it]) }
            Butt.Next(bandX != null && bandY != null && bandX != bandY) {
                viewModel.bandX = bandX!!
                viewModel.bandY = bandY!!
                next()
            }
        }
    }
}