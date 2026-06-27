package org.blueventures.gemdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.model.Licenses
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.model.settings.SettingsViewModel
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarState
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.BasicActions
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.roi.Roi
import org.blueventures.gemdroid.ui.settings.AppSettings
import org.blueventures.gemdroid.ui.signin.SignIn
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme
import org.blueventures.gemdroid.ui.theme.OffWhite
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.welcome.Welcome

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Licenses.set(this)

        setContent {
            GEMApp(this)
        }
    }
}

@Composable
fun GEMApp(activity: ComponentActivity) {
    val settingsModel: SettingsViewModel by activity.viewModels()
    val roiModel: RoiViewModel by activity.viewModels()
    val analysisModel: AnalysisViewModel by activity.viewModels()
    analysisModel.init(activity)

    val (welcomeShown, setWelcomeShown) = remember { mutableStateOf<Boolean?>(null) }
    when (welcomeShown) {
        null -> {
            settingsModel.getWelcomeShown(activity, setWelcomeShown)
        }
        else -> {
            GEMTheme(activity, settingsModel, analysisModel, roiModel, welcomeShown)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GEMTheme(activity: ComponentActivity, settingsModel: SettingsViewModel, analysisModel: AnalysisViewModel, roiModel: RoiViewModel, welcomeShown: Boolean) {
    val nav = rememberNavController()
    val snackHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snack: SnackFun = { msg ->
        scope.launch {
            snackHostState.currentSnackbarData?.dismiss()
            snackHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    GEMDroidTheme {
        val (barState, setBarState) = remember { mutableStateOf(AppBarState(
            settings = { nav.navigate(AppSettings.Routes.settings) },
            signOut = { SignIn.signOut(activity, Firebase.auth) },
        )) }

        val appBar = AppBar { update ->
            LaunchedEffect(true) {
                if (update.title != barState.update.title || update.actions != null || barState.update.actions != null) {
                    setBarState(barState.copy(update = AppBarUpdate(update.title, false, update.actions ?: if (update.logoutOnly) {
                        { BasicActions(null, barState.signOut) }
                    } else {
                        { BasicActions(barState.settings, barState.signOut)  }
                    })))
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text(barState.update.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    actions = { barState.update.actions?.invoke(this) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBlue, titleContentColor = OffWhite, actionIconContentColor = OffWhite)
                )
            },
            snackbarHost = { SnackbarHost(snackHostState) }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = if (welcomeShown) Roi.Routes.list else Welcome.Routes.landing,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Welcome.screens(this, nav, activity, settingsModel, appBar)
                AppSettings.screens(this, nav, settingsModel, appBar)
                Roi.screens(this, nav, activity, roiModel, analysisModel, appBar, snack)
                Analysis.screens(this, nav, analysisModel, roiModel, appBar, snack)
            }
        }
    }
}