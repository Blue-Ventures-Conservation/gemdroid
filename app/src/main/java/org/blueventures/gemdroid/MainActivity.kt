package org.blueventures.gemdroid

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.roi.Roi
import org.blueventures.gemdroid.ui.roi.RoiRoutes
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme
import org.blueventures.gemdroid.ui.todo.Todo
import org.blueventures.gemdroid.ui.todo.Todoer

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
    val roiViewModel: RoiViewModel by activity.viewModels()
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
                // ROI list
                composable(RoiRoutes.list) {
                    Roi.List(roiViewModel, activity.filesDir, snackbar, roiClick = { dir ->

                    }) {
                        nav.navigate(RoiRoutes.name)
                    }
                }

                // ROI name creation
                composable(RoiRoutes.name) {
                    Roi.Name(roiViewModel, snackbar, backClick = {
                        roiViewModel.clear()
                        nav.popBackStack()
                    }) {
                        nav.navigate(RoiRoutes.contemporaryYears)
                    }
                }

                // ROI contemporary years selection
                composable(RoiRoutes.contemporaryYears) {
                    Roi.ContemporaryDates(roiViewModel, snackbar, backClick = {
                        roiViewModel.clearContemporaryYears()
                        nav.popBackStack()
                    }) {
                        nav.navigate(RoiRoutes.historicalYears)
                    }
                }

                // ROI historical years selection
                composable(RoiRoutes.historicalYears) {
                    Roi.HistoricalDates(roiViewModel, snackbar, backClick = {
                        roiViewModel.clearHistoricalYears()
                        nav.popBackStack()
                    }) {
                        nav.navigate(RoiRoutes.months)
                    }
                }

                // ROI months range selection
                composable(RoiRoutes.months) {
                    Roi.Months(roiViewModel, snackbar, backClick = {
                        roiViewModel.clearMonths()
                        nav.popBackStack()
                    }) {
                        nav.navigate(RoiRoutes.indices)
                    }
                }

                // ROI indices selection
                composable(RoiRoutes.indices) {
                    Roi.Indices(roiViewModel, backClick = {
                        roiViewModel.clearIndices()
                        nav.popBackStack()
                    }) {
                        nav.navigate(RoiRoutes.polygon)
                    }
                }

                // ROI polygon creation
                composable(RoiRoutes.polygon) {
                    Roi.Polygon(roiViewModel, snackbar, backClick = {
                        roiViewModel.clearPoints()
                        nav.popBackStack()
                    }) {
                        nav.navigate(RoiRoutes.overview)
                    }
                }

                // ROI overview
                composable(RoiRoutes.overview) {
                    Roi.Overview(roiViewModel, activity.filesDir, snackbar) {
                        roiViewModel.clear()
                        nav.popUpTo(RoiRoutes.list)
                    }
                }

                composable(Todoer.route) {
                    Todo {
                        Firebase.auth.signOut()
                        val intent = Intent(activity, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
            }
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) { launchSingleTop = true }

fun NavHostController.popUpTo(route: String) =
    this.navigate(route) { popUpTo(route) { inclusive = true } }