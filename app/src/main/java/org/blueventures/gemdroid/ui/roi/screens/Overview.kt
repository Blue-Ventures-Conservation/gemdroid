package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.File

object Overview {
    @Composable
    fun Screen(viewModel: RoiViewModel, filesDir: File, appBar: AppBar, snack: SnackFun, back: Click, done: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
            val (saving, setSaving) = remember { mutableStateOf(false) }

            if (saving) {
                Progress()
            } else {
                OverviewDetails(viewModel, filesDir, snack, done, setSaving)
            }
        }
    }

    @Composable
    fun OverviewDetails(viewModel: RoiViewModel, filesDir: File, snack: SnackFun, done: Click, saving: (Boolean) -> Unit) {
        Col.MidPad {
            Info.Block {
                Info.Header(title = stringResource(id = R.string.overview))
                Info.BlueLine()
                OverviewRow(stringResource(R.string.overview_name), viewModel.name)
                OverviewRow(stringResource(R.string.overview_contemporary_years), "${viewModel.contemporaryYearStart} - ${viewModel.contemporaryYearEnd}")
                OverviewRow(stringResource(R.string.overview_contemporary_months), "${viewModel.contemporaryMonthStart} - ${viewModel.contemporaryMonthEnd}")
                OverviewRow(stringResource(R.string.overview_historical_years), "${viewModel.historicalYearStart} - ${viewModel.historicalYearEnd}")
                OverviewRow(stringResource(R.string.overview_historical_months), "${viewModel.historicalMonthStart} - ${viewModel.historicalMonthEnd}")
                OverviewRow(stringResource(R.string.overview_polygon_points), stringResource(R.string.overview_points).format(viewModel.drawPoly.points.size.toString()))
                OverviewRow(stringResource(R.string.overview_polygon_area), viewModel.drawPoly.polygonSquareKms())
            }
            val ctx = LocalContext.current
            Butt.Done {
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

    @Composable
    fun OverviewRow(label: String, value: String) {
        Info.Row(verticalPadding = 16.dp) {
            Info.Txt(label)
            Info.Txt(value)
        }
        Info.BlueLine()
    }
}