package org.blueventures.gemdroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.blueventures.gemdroid.ui.signin.SignIn
import org.blueventures.gemdroid.ui.signin.Signer
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.auth.currentUser?.let {
            signedIn()
        }

        Signer.registerForSignIn(this, Firebase.auth) { user ->
            user?.let {
                signedIn()
            } ?: run {
                content(true)
            }
        }

        content(false)
    }

    private fun content(failed: Boolean) {
        setContent {
            GEMDroidTheme {
                SignIn(failed) {
                    Signer.doSignIn(this)
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