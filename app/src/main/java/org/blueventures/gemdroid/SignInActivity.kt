package org.blueventures.gemdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.blueventures.gemdroid.ui.signin.SignIn
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.auth.currentUser?.let {
            SignIn.signedIn(this)
        }

        content()
    }

    private fun content() {
        setContent {
            GEMDroidTheme {
                SignIn.Screen(this)
            }
        }
    }
}