package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Collect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun

object NameRegion {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBarFun, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap({
            viewModel.regionName = ""
            back()
        }, next) { nav ->
            appBar(AppBarUpdate(viewModel.roi.appBar(stringResource(R.string.dynamics))))
            val err = stringResource(R.string.please_enter_without_special_chars)
            Collect.Text(header = stringResource(R.string.name_your_sub_region), label = stringResource(R.string.please_enter_name), initial = viewModel.regionName, snack, { name ->
                viewModel.regionName = name
                if (viewModel.validateRegionName()) null else err
            }, nav::next)
        }
    }
}