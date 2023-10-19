package org.blueventures.gemdroid

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.model.Licenses
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarState
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.BasicActions
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.roi.Roi
import org.blueventures.gemdroid.ui.signin.SignIn
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme
import org.blueventures.gemdroid.ui.theme.OffWhite
import org.blueventures.gemdroid.ui.theme.SkyBlue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Licenses.set(this)

        setContent {
            GEMApp(this)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GEMApp(activity: ComponentActivity) {
    val roiModel: RoiViewModel by activity.viewModels()
    val analysisModel: AnalysisViewModel by activity.viewModels()
    analysisModel.init(activity)

    val nav = rememberNavController()
    val snackHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snack: SnackFun = { msg ->
        scope.launch {
            snackHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    GEMDroidTheme {
        val (barState, setBarState) = remember { mutableStateOf(AppBarState(
            // add settings nav here
            signOut = { SignIn.signOut(activity, Firebase.auth) },
        )) }

        val appBar = AppBar {
            LaunchedEffect(true) {
                if (it.title != barState.update.title || it.actions != null) {
                    val acts: @Composable (RowScope.() -> Unit) = it.actions ?: { BasicActions(barState.signOut) }
                    setBarState(barState.copy(update = AppBarUpdate(it.title, acts)))
                }
            }
        }

        Scaffold(
            topBar = {
                     TopAppBar(
                         modifier = Modifier.fillMaxWidth(),
                         title = { Text(barState.update.title) },
                         actions = { barState.update.actions?.invoke(this) },
                         colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBlue, titleContentColor = OffWhite, actionIconContentColor = OffWhite)
                     )
            },
            snackbarHost = { SnackbarHost(snackHostState) }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Roi.Routes.list,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Roi.screens(this, nav, activity, roiModel, analysisModel, appBar, snack)
                Analysis.screens(this, nav, analysisModel, appBar, snack)
            }
        }
    }
}

fun NavHostController.popClear(route: String) {
    val controller = this
    navigate(route) {
        popUpTo(controller.graph.id) {
            inclusive = true
        }
    }
}