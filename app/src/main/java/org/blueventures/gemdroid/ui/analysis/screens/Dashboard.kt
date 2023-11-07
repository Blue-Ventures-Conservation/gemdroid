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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Divider
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
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.Roi
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Dashboard {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun, next: Click, back: Click, vis: Click, clazz: Click, dyn: Click) {
        Nav.Wrap(back) {
            Layout(viewModel, appBar, snack, {
                next()
            }, back, {
                vis()
            }, {
                clazz()
            }) {
                dyn()
            }
        }
    }

    @Composable
    fun Layout(viewModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun, next: Click, back: Click, vis: Click, clazz: Click, dyn: Click) {
        appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.analysis))))
        val (stage, setStage) = remember { mutableStateOf<Stage?>(null) }

        Roi.Loader(viewModel::getROI, snack, {
            back()
        }) {
            viewModel.roi = it

            when (stage) {
                null -> {
                    Progress()
                    viewModel.refreshStage(setStage)
                }
                else -> {
                    viewModel.stage = stage
                    Dashboard(stage, snack, next, back, vis, clazz, dyn)
                }
            }
        }
    }

    @Composable
    fun Dashboard(stage: Stage, snack: SnackFun, next: Click, back: Click, vis: Click, clazz: Click, dyn: Click) {
        Col.Col {
            when (stage) {
                Stage.ERROR -> {
                    val msg = stringResource(R.string.could_not_read_fs)
                    Effect.Once {
                        snack(msg)
                        back()
                    }
                }
                Stage.BUFFER -> {
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        text = "Click Next to start!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (stage) {
                            Stage.CRAS -> {
                                VisualizeRow(vis)
                            }
                            Stage.ALL -> {
                                VisualizeRow(vis)
                                ClassificationRow(clazz)
                                DynamicsRow(dyn)
                            }
                            else -> {}
                        }
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
    fun VisualizeRow(vis: Click) {
        DashboardRow("Visualize", vis)
    }

    @Composable
    fun SeparabilityRow(sep: Click) {
        DashboardRow("Spectral Separability", sep)
    }

    @Composable
    fun ClassificationRow(clazz: Click) {
        DashboardRow("Classification", clazz)
    }

    @Composable
    fun DynamicsRow(dyn: Click) {
        DashboardRow("Dynamics", dyn)
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
                Icons.Filled.ArrowForward, "Go to $title", modifier = Modifier
                    .padding(20.dp)
                    .size(32.dp))
        }
        Divider(color = SkyBlue, thickness = 1.dp)
    }
}