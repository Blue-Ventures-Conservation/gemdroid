package org.blueventures.gemdroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme
import org.blueventures.gemdroid.ui.todo.Todo
import org.blueventures.gemdroid.ui.todo.Todoer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GEMApp(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GEMApp(activity: Activity) {
    GEMDroidTheme {
        val nav = rememberNavController()
        Scaffold { innerPadding ->
            NavHost(
                navController = nav,
                startDestination = Todoer.route,
                modifier = Modifier.padding(innerPadding)
            ) {
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

fun NavHostController.popUpTo(popTo: String, navTo: String) =
    this.navigate(navTo) { popUpTo(popTo) }