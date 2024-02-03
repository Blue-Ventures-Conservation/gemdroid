package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.model.roi.RoiDatasource
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Collect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun

object CombinedName {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap({
            viewModel.combinedName = null
            back()
        }) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
            val err = stringResource(R.string.please_enter_unique_non_special_name).format(RoiDatasource.maxNameCharLength.toString())
            Collect.Text(stringResource(R.string.create_a_combined_name), stringResource(R.string.please_enter_name), initial = viewModel.combinedName ?: "", snack, { name ->
                if (viewModel.validateCombinedName(name)) {
                    viewModel.combinedName = name
                    null
                } else err
            }, next)
        }
    }
}