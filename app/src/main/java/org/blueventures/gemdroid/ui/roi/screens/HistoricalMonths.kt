package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click

object HistoricalMonths {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, next: Click) {
        val (forceLandsat, setForceLandsat) = remember { mutableStateOf<Boolean?>(null) }
        val ctx = LocalContext.current

        Months.Screen(
            object : Months.Selector {
                override val initMonthStart: Int = viewModel.historicalMonthStart
                override val initMonthEnd: Int = viewModel.historicalMonthEnd
                override fun setMonthStart(month: Int) {
                    viewModel.historicalMonthStart = month
                }

                override fun setMonthEnd(month: Int) {
                    viewModel.historicalMonthEnd = month
                }
            }, stringResource(R.string.historical), appBar
        ) {
            viewModel.getForceLandsat(ctx, setForceLandsat)
        }

        forceLandsat?.let { force ->
            ImageryDialog(viewModel, viewModel.shouldUseS2(), force, next)
        }
    }

    @Composable
    fun ImageryDialog(viewModel: RoiViewModel, useS2: Boolean, forceLandsat: Boolean, next: Click) {
        val starts = if (useS2) stringResource(R.string.starts_after_or_during) else stringResource(R.string.starts_before)
        val but = if (useS2 && forceLandsat) stringResource(R.string.but_force_landsat) else ""
        val imagery = if (useS2 && !forceLandsat) stringResource(R.string.use_sentinel_2) else stringResource(R.string.use_landsat)
        val warning = if (useS2 && !forceLandsat) stringResource(R.string.sentinel_2_warning) else ""

        viewModel.forceLS = forceLandsat
        AlertDialog(
            onDismissRequest = next,
            title = { Text(text = stringResource(R.string.imagery)) },
            text = { Text(text = "$starts$but$imagery$warning") },
            confirmButton = {
                Butt.Text(stringResource(android.R.string.ok), click = next)
            },
        )
    }
}