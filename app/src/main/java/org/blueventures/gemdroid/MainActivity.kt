package org.blueventures.gemdroid

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.AnalysisRoutes
import org.blueventures.gemdroid.ui.roi.Roi
import org.blueventures.gemdroid.ui.roi.RoiRoutes
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GEMApp(this)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GEMApp(activity: ComponentActivity) {
    val analysisModel: AnalysisViewModel by activity.viewModels()
    val roiModel: RoiViewModel by activity.viewModels()
    val nav = rememberNavController()
    val snackHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbar: (String) -> Unit = { msg ->
        scope.launch {
            snackHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    GEMDroidTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackHostState) }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = RoiRoutes.list,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                roiBuilder(this, nav, activity, roiModel, analysisModel, snackbar)
                analysisBuilder(this, nav, analysisModel, snackbar)
            }
        }
    }
}

fun analysisBuilder(b: NavGraphBuilder, nav: NavHostController, viewModel: AnalysisViewModel, snackbar: (String) -> Unit) {
    // Dashboard
    b.composable(AnalysisRoutes.dashboard) {
        Analysis.Dashboard(viewModel, snackbar, nextClick = { stage ->
            AnalysisRoutes.dashboardNext(stage)?.let { route ->
                nav.navigate(route)
            }
        }, backClick = {
            viewModel.clear()
            nav.popBackStack()
        }, visClick = {
            nav.navigate(AnalysisRoutes.visualize)
        }, sepClick = {
            nav.navigate(AnalysisRoutes.separabilityDashboard)
        }, classClick = {
            nav.navigate(AnalysisRoutes.classification)
        }, dynClick = {
            nav.navigate(AnalysisRoutes.dynamics)
        })
    }

    b.composable(AnalysisRoutes.buffer) {
        // TODO
    }
}

fun roiBuilder(b: NavGraphBuilder, nav: NavHostController, activity: ComponentActivity, roiModel: RoiViewModel, analysisModel: AnalysisViewModel, snackbar: (String) -> Unit) {
    // ROI list
    b.composable(RoiRoutes.list) {
        Roi.List(roiModel, activity.filesDir, snackbar, roiClick = { dir ->
            analysisModel.setRoiDir(dir)
            nav.navigate(AnalysisRoutes.dashboard)
        }) {
            nav.navigate(RoiRoutes.name)
        }
    }

    // ROI name creation
    b.composable(RoiRoutes.name) {
        Roi.Name(roiModel, snackbar, backClick = {
            roiModel.clear()
            nav.popBackStack()
        }) {
            nav.navigate(RoiRoutes.contemporaryYears)
        }
    }

    // ROI contemporary years selection
    b.composable(RoiRoutes.contemporaryYears) {
        Roi.ContemporaryDates(roiModel, snackbar, backClick = {
            roiModel.clearContemporaryYears()
            nav.popBackStack()
        }) {
            nav.navigate(RoiRoutes.historicalYears)
        }
    }

    // ROI historical years selection
    b.composable(RoiRoutes.historicalYears) {
        Roi.HistoricalDates(roiModel, snackbar, backClick = {
            roiModel.clearHistoricalYears()
            nav.popBackStack()
        }) {
            nav.navigate(RoiRoutes.months)
        }
    }

    // ROI months range selection
    b.composable(RoiRoutes.months) {
        Roi.Months(roiModel, snackbar, backClick = {
            roiModel.clearMonths()
            nav.popBackStack()
        }) {
            nav.navigate(RoiRoutes.indices)
        }
    }

    // ROI indices selection
    b.composable(RoiRoutes.indices) {
        Roi.Indices(roiModel, backClick = {
            roiModel.clearIndices()
            nav.popBackStack()
        }) {
            nav.navigate(RoiRoutes.polygon)
        }
    }

    // ROI polygon creation
    b.composable(RoiRoutes.polygon) {
        Roi.Polygon(roiModel, snackbar, backClick = {
            roiModel.clearPoints()
            nav.popBackStack()
        }) {
            nav.navigate(RoiRoutes.overview)
        }
    }

    // ROI overview
    b.composable(RoiRoutes.overview) {
        Roi.Overview(roiModel, activity.filesDir, snackbar) {
            roiModel.clear()
            nav.popUpTo(RoiRoutes.list)
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) { launchSingleTop = true }

fun NavHostController.popUpTo(route: String) =
    this.navigate(route) { popUpTo(route) { inclusive = true } }