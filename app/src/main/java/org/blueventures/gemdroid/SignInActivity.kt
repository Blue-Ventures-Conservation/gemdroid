package org.blueventures.gemdroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.blueventures.gemdroid.ui.signin.SignIn
import org.blueventures.gemdroid.ui.signin.SignInScreen
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.auth.currentUser?.let {
            signedIn()
        }

        SignIn.registerForSignIn(this, Firebase.auth) { user ->
            user?.let {
                signedIn()
            } ?: run {
                content(true)
            }
        }

        content(false)
    }

    private fun content(failed: Boolean) {
        var msg: String? = null
        if (failed) {
            msg = getString(R.string.sign_in_failed)
        }

        setContent {
            GEMDroidTheme {
                SignInScreen(msg) {
                    SignIn.doSignIn(this)
                }
            }
        }
    }

    private fun signedIn() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}