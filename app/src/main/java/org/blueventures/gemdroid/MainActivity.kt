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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.roi.Roi
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
                startDestination = Roi.Routes.list,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Roi.screens(this, nav, activity, roiModel, analysisModel, snackbar)
                Analysis.screens(this, nav, analysisModel, snackbar)
            }
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) { launchSingleTop = true }

fun NavHostController.popUpTo(route: String) =
    this.navigate(route) { popUpTo(route) { inclusive = true } }