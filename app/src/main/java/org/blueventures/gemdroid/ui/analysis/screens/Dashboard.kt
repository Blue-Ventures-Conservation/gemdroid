package org.blueventures.gemdroid.ui.analysis.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource.Companion.roiUUID
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.Roi
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.once
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Dashboard {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click, falseColor: Click, review: Click, clazz: Click, dyn: Click) {
        Effect.Once { viewModel.dynamicsViewModel.polygons.clear() }
        Layout(viewModel, appBar, snack, back, next, falseColor, review, clazz, dyn)
    }

    @Composable
    fun Layout(viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click, falseColor: Click, properties: Click, clazz: Click, dyn: Click) {
        Roi.Loader(viewModel::getROI, snack, {
            back()
        }) {
            var roi = it
            if (it.regionUUID == null) {
                roi = it.copy(regionUUID = roiUUID())
                viewModel.saveROI(roi)
            }
            viewModel.roi = roi
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.analysis))))

            val (stage, setStage) = remember { mutableStateOf<Stage?>(null) }
            when (stage) {
                null -> {
                    Progress()
                    viewModel.refreshStage(setStage)
                }
                else -> {
                    viewModel.stage = stage
                    Dashboard(viewModel.roi.name, stage, snack, back, next, falseColor, properties, clazz, dyn)
                }
            }
        }
    }

    @Composable
    fun BackgroundPrompt(text: String) {
        Text(
            text = text,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        )
    }

    @Composable
    fun Dashboard(roiName: String, stage: Stage, snack: SnackFun, back: Click, next: Click, falseColor: Click, properties: Click, clazz: Click, dyn: Click) {
        Col.Col {
            when (stage) {
                Stage.ERROR -> {
                    val msg = stringResource(R.string.could_not_read_fs)
                    snack.once(msg)
                    back.once()
                }
                Stage.BUFFER -> {
                    Spacer(modifier = Modifier.height(0.dp))
                    BackgroundPrompt(stringResource(R.string.click_next_to_start_s_analysis).format(roiName))
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (stage) {
                            Stage.COMPOSITES -> {
                                SatelliteRow(falseColor)
                                ReviewInputsRow(properties)
                            }
                            Stage.CRAS -> {
                                SatelliteRow(falseColor)
                                ReviewInputsRow(properties)
                            }
                            Stage.CLASSIFICATION -> {
                                SatelliteRow(falseColor)
                                ClassificationRow(clazz)
                                ReviewInputsRow(properties)
                            }
                            Stage.ALL -> {
                                SatelliteRow(falseColor)
                                ClassificationRow(clazz)
                                DynamicsRow(dyn)
                                ReviewInputsRow(properties)
                            }
                            else -> {}
                        }
                    }

                    when (stage) {
                        Stage.COMPOSITES -> BackgroundPrompt(stringResource(R.string.click_next_to_view_and_assess))
                        Stage.CRAS -> BackgroundPrompt(stringResource(R.string.click_next_to_add_cras).format(roiName))
                        else -> {}
                    }
                }
            }

            if (stage != Stage.ALL) {
                DashboardNextButton {
                    next()
                }
            }
        }
    }

    @Composable
    fun DashboardNextButton(next: Click) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Butt.Next(click = next)
        }
    }

    @Composable
    fun SatelliteRow(falseColor: Click) {
        DashboardRow(stringResource(R.string.visualize_imagery_title), falseColor)
    }

    @Composable
    fun ReviewInputsRow(properties: Click) {
        DashboardRow(stringResource(R.string.review_inputs), properties)
    }

    @Composable
    fun ClassificationRow(clazz: Click) {
        DashboardRow(stringResource(R.string.classification), clazz)
    }

    @Composable
    fun DynamicsRow(dyn: Click) {
        DashboardRow(stringResource(R.string.dynamics), dyn)
    }

    @Composable
    fun DashboardRow(title: String, row: Click) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { row() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 24.sp, modifier = Modifier.padding(24.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, "Go to $title", modifier = Modifier
                    .padding(20.dp)
                    .size(32.dp))
        }
        HorizontalDivider(color = SkyBlue, thickness = 1.dp)
    }
}