package org.blueventures.gemdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.blueventures.gemdroid.ui.signin.SignIn
import org.blueventures.gemdroid.ui.theme.GEMDroidTheme

class SignInActivity : ComponentActivity() {
    private var setMsg: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.auth.currentUser?.let {
            SignIn.signedIn(this)
        }

        SignIn.registerForSignIn(this, Firebase.auth) { user ->
            user?.let {
                SignIn.signedIn(this)
            } ?: run {
                msgFunc(getString(R.string.sign_in_failed))
            }
        }

        content()
    }

    private fun msgFunc(msg: String) = setMsg(msg)

    private fun content() {
        setContent {
            GEMDroidTheme {
                val (msg, msgSet) = remember { mutableStateOf("") }
                setMsg = msgSet
                SignIn.Screen(msg) {
                    setMsg("")
                    SignIn.doSignIn(this)
                }
            }
        }
    }
}