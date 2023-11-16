package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxNameCharLength
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Collect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun

object Name {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            viewModel.filesDir = LocalContext.current.filesDir

            appBar.Update(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
            val err = stringResource(R.string.please_enter_unique_non_special_name).format(maxNameCharLength.toString())
            Collect.Text(header = stringResource(R.string.name_your_roi), label = stringResource(R.string.please_enter_name), initial = viewModel.roiName, snack, { name ->
                viewModel.roiName = name
                if (viewModel.notTooLong() && viewModel.notSpecial() && viewModel.isUnique()) null else err
            }, next)
        }
    }
}