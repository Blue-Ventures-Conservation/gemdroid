package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.Roi.OverviewFromState
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.File

object Overview {
    @Composable
    fun Screen(viewModel: RoiViewModel, filesDir: File, appBar: AppBar, snack: SnackFun, done: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
        val (saving, setSaving) = remember { mutableStateOf(false) }

        if (saving) {
            Progress()
        } else {
            OverviewDetails(viewModel, filesDir, snack, done, setSaving)
        }
    }

    @Composable
    fun OverviewDetails(viewModel: RoiViewModel, filesDir: File, snack: SnackFun, done: Click, saving: (Boolean) -> Unit) {
        val ctx = LocalContext.current
        OverviewFromState(
            viewModel.roiName,
            viewModel.contemporaryYearStart,
            viewModel.contemporaryYearEnd,
            viewModel.contemporaryMonthStart,
            viewModel.contemporaryMonthEnd,
            viewModel.historicalYearStart,
            viewModel.historicalYearEnd,
            viewModel.historicalMonthStart,
            viewModel.historicalMonthEnd,
            viewModel.multiPolyFromState(),
            viewModel.polygons.map { it.polygon },
            (!viewModel.forceLS && viewModel.shouldUseS2()),
            stringResource(id = R.string.overview),
            stringResource(id = R.string.done_button),
        ) {
            saving(true)
            viewModel.saveRoi(filesDir) { result ->
                saving(false)
                if (result.isSuccess) {
                    done()
                } else {
                    snack(result.exceptionOrNull()!!.localized(ctx))
                }
            }
        }
    }
}