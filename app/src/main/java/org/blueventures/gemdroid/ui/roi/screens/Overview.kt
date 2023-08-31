package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import java.io.File

object Overview {
    @Composable
    fun Screen(viewModel: RoiViewModel, filesDir: File, appBar: AppBarFun, snack: SnackFun, back: Click, done: Click) {
        Nav.Wrap(back, done) { nav ->
            appBar(AppBarUpdate(stringResource(R.string.create_coarse_roi)))
            val (saving, setSaving) = remember { mutableStateOf(false) }

            if (saving) {
                Progress()
            } else {
                OverviewDetails(viewModel, filesDir, snack, nav::next, setSaving)
            }
        }
    }

    @Composable
    fun OverviewDetails(viewModel: RoiViewModel, filesDir: File, snack: SnackFun, done: Click, saving: (Boolean) -> Unit) {
        Col.MidPad {
            Text(text = stringResource(R.string.overview), fontSize = 32.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = stringResource(R.string.overview_name), fontSize = 18.sp)
                    Text(text = stringResource(R.string.overview_contemporary_years), fontSize = 18.sp)
                    Text(text = stringResource(R.string.overview_contemporary_months), fontSize = 18.sp)
                    Text(text = stringResource(R.string.overview_historical_years), fontSize = 18.sp)
                    Text(text = stringResource(R.string.overview_historical_months), fontSize = 18.sp)
                    Text(text = stringResource(R.string.overview_polygon_roi), fontSize = 18.sp)
                }
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = viewModel.name, fontSize = 18.sp)
                    Text(text = "${viewModel.contemporaryYearStart} - ${viewModel.contemporaryYearEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.contemporaryMonthStart} - ${viewModel.contemporaryMonthEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.historicalYearStart} - ${viewModel.historicalYearEnd}", fontSize = 18.sp)
                    Text(text = "${viewModel.historicalMonthStart} - ${viewModel.historicalMonthEnd}", fontSize = 18.sp)
                    Text(text = stringResource(R.string.overview_points).format(viewModel.drawPoly.points.size.toString(), viewModel.drawPoly.polygonSquareKms()), fontSize = 18.sp)
                }
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
}