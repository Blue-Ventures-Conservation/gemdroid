package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.dynamics.SubRegionsFile
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsDatasource.Companion.maxSubRegions
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object SubRegionsOption {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun, skip: Click, yes: Click, no: Click, back: Click) {
        Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras_dynamics), viewModel.craAwaiter) { cra ->
            viewModel.cra = cra
            Choice(viewModel, appBar, snack, skip, yes, no, back)
        }
    }

    @Composable
    fun Choice(viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun, skip: Click, yes: Click, no: Click, back: Click) {
        appBar(AppBarUpdate(stringResource(R.string.dynamics)))

        val (subRegions, setSubRegions) = remember { mutableStateOf<Result<SubRegionsFile>?>(null) }

        when {
            subRegions == null -> {
                Progress()
                viewModel.loadSubRegionsFile(setSubRegions)
            }
            subRegions.isFailure -> Layout(viewModel, snack, yes, no)
            else -> {
                viewModel.subRegions.clear()
                viewModel.subRegions.addAll(subRegions.getOrNull()!!.subRegions)
                viewModel.subRegionsLoaded = true
                skip()
            }
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun Layout(viewModel: DynamicsViewModel, snack: SnackFun, yes: Click, no: Click) {
        var header = R.string.would_you_like_subregions
        var yesButton = R.string.yes_add_sub_regions
        var noButton = R.string.no_skip_sub_regions

        if (viewModel.subRegions.isNotEmpty()) {
            header = R.string.would_you_like_more_sub_regions
            yesButton = R.string.yes_add_another_sub_region
            noButton = R.string.no_skip_more_sub_regions
        }

        Col.Dash(stringResource(header)) {
            val tooMany = stringResource(R.string.please_hit_no)
            DashboardButton(stringResource(yesButton)) {
                if (viewModel.subRegions.size < maxSubRegions) {
                    yes()
                } else {
                    snack(tooMany)
                }
            }
            DashboardButton(stringResource(noButton)) {
                viewModel.saveSubRegionsFile()
                no()
            }
        }
    }
}