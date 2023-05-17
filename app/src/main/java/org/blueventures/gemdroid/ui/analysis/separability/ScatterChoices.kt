package org.blueventures.gemdroid.ui.analysis.separability

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.blueventures.gemdroid.data.JSONMap
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
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

        GetRemote.AwaitSave(viewModel::loadScatterFile, viewModel::getScatter, viewModel::saveScatterFile, Separability::craErrorHandler) { json ->
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
        Col.Between {
            Dropdown(title = "Select X Band", labels = bands) { setBandX(bands[it]) }
            Dropdown(title = "Select Y Band", labels = bands) { setBandY(bands[it]) }
            Butt.Next(bandX != null && bandY != null && bandX != bandY) {
                viewModel.bandX = bandX!!
                viewModel.bandY = bandY!!
                next()
            }
        }
    }
}