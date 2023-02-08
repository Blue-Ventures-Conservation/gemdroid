package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.analysis.Stage
import org.blueventures.gemdroid.ui.theme.SkyBlue

object Analysis {
    @Composable
    fun Dashboard(viewModel: AnalysisViewModel, snackbar: (String) -> Unit, nextClick: (Stage) -> Unit, backClick: () -> Unit, visClick: () -> Unit, sepClick: () -> Unit, classClick: () -> Unit, dynClick: () -> Unit) {
        val state by viewModel.state.collectAsState()

        if (state.stage == null) {
            Progress()
            viewModel.refreshStage()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Header("${state.roiDir.name} Analysis")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, bottom = 64.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val stage = state.stage!!
                    when (stage) {
                        Stage.ERROR -> {
                            snackbar("Could not read filesystem state!")
                            backClick()
                        }
                        Stage.BUFFER -> {
                            Spacer(modifier = Modifier.height(0.dp))
                            Text(
                                text = "Click Next to start!",
                                fontSize = 18.sp,
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
                                    Stage.VISUALIZE -> {
                                        VisualizeRow(visClick)
                                    }
                                    Stage.COLORS -> {
                                        VisualizeRow(visClick)
                                    }
                                    Stage.CRAS -> {
                                        VisualizeRow(visClick)
                                    }
                                    Stage.SEPARABILITY -> {
                                        VisualizeRow(visClick)
                                        SeparabilityRow(sepClick)
                                    }
                                    Stage.CLASSIFICATION -> {
                                        VisualizeRow(visClick)
                                        SeparabilityRow(sepClick)
                                        ClassificationRow(classClick)
                                    }
                                    Stage.COUNTRY -> {
                                        VisualizeRow(visClick)
                                        SeparabilityRow(sepClick)
                                        ClassificationRow(classClick)
                                    }
                                    Stage.DYNAMICS -> {
                                        VisualizeRow(visClick)
                                        SeparabilityRow(sepClick)
                                        ClassificationRow(classClick)
                                        DynamicsRow(dynClick)
                                    }
                                    Stage.DONE -> {
                                        VisualizeRow(visClick)
                                        SeparabilityRow(sepClick)
                                        ClassificationRow(classClick)
                                        DynamicsRow(dynClick)
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }

                    if (stage != Stage.DONE) {
                        DashboardNextButton {
                            nextClick(stage)
                        }
                    }
                }
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun DashboardNextButton(nextClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    nextClick()
                }
            ) {
                Text("Next", fontSize = 18.sp)
            }
        }
    }

    @Composable
    fun VisualizeRow(visClick: () -> Unit) {
        DashboardRow("Visualize", visClick)
    }

    @Composable
    fun SeparabilityRow(sepClick: () -> Unit) {
        DashboardRow("Spectral Separability", sepClick)
    }

    @Composable
    fun ClassificationRow(classClick: () -> Unit) {
        DashboardRow("Classification", classClick)
    }

    @Composable
    fun DynamicsRow(dynClick: () -> Unit) {
        DashboardRow("Dynamics", dynClick)
    }

    @Composable
    fun DashboardRow(title: String, rowClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rowClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 24.sp, modifier = Modifier.padding(24.dp))
            Icon(Icons.Filled.ArrowForward, "Go to $title", modifier = Modifier
                .padding(20.dp)
                .size(32.dp))
        }
        Divider(color = SkyBlue, thickness = 1.dp)
    }

    @Composable
    fun Progress() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(152.dp))
        }
    }

    @Composable
    fun Header(title: String) {
        Text(
            text = title, fontSize = 24.sp, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 24.dp, bottom = 32.dp)
        )
        Divider(color = SkyBlue, thickness = 4.dp)
    }
}

object AnalysisRoutes {
    const val dashboard = "dashboard"
    const val buffer = "buffer"
    const val visualize = "visualize"
    const val colors = "colors"
    const val cont_cra = "cont_cra"
    const val hist_cra = "hist_cra"
    const val separabilityDashboard = "sep_dashboard"
    const val correlation = "corr"
    const val lsSeparation = "ls_sep"
    const val indicesSeparation = "indices_sep"
    const val classification = "classification"
    const val classification_map = "class_map"
    const val country = "country"
    const val dynamics = "dynamics"
    const val dynamics_map = "dyn_map"

    fun dashboardNext(stage: Stage): String? {
        return when(stage) {
            Stage.BUFFER -> buffer
            Stage.VISUALIZE -> visualize
            Stage.COLORS -> colors
            Stage.CRAS -> cont_cra
            Stage.SEPARABILITY -> separabilityDashboard
            Stage.CLASSIFICATION -> classification
            Stage.COUNTRY -> country
            Stage.DYNAMICS -> dynamics
            else -> null
        }
    }
}