package org.blueventures.gemdroid.ui.signin

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.MainActivity
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.SignInActivity
import org.blueventures.gemdroid.ui.common.Butt

object SignIn {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(activity: Activity) {
        val snackHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val snackbar: (String) -> Unit = { msg ->
            scope.launch {
                snackHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            }
        }
        val (msg, setMsg) = remember { mutableStateOf("") }

        val oneTapClient = Identity.getSignInClient(activity)
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            onSignInResult(activity, result, oneTapClient, Firebase.auth) { user ->
                user?.let {
                    signedIn(activity)
                } ?: run {
                    setMsg(activity.getString(R.string.sign_in_failed))
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackHostState) }
        ) { padding ->
            if (msg.isNotEmpty()) {
                snackbar(msg)
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.sign_in_prompt),
                    modifier = Modifier.padding(48.dp),
                )
                Butt.Text(stringResource(R.string.sign_in_label)) {
                    setMsg("")
                    doSignIn(activity, oneTapClient, launcher)
                }
            }
        }
    }

    private const val TAG = "Signer"

    private fun doSignIn(activity: Activity, oneTapClient: SignInClient, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(false).build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(activity.getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in?
                    .setFilterByAuthorizedAccounts(false).build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(false).build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(activity) { result ->
                try {
                    launcher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "could not send intent: ${e.message}")
                }
            }
            .addOnFailureListener(activity) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.e(TAG, "no saved credentials found: ${e.message}")
            }
    }

    private fun onSignInResult(activity: Activity, result: ActivityResult, oneTapClient: SignInClient, auth: FirebaseAuth, setUser: (FirebaseUser?) -> Unit) {
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            setUser(auth.currentUser)
                        } else {
                            setUser(null)
                        }
                    }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d(TAG, "One-tap dialog was closed.")
                    }
                    CommonStatusCodes.NETWORK_ERROR -> {
                        Log.d(TAG, "One-tap encountered a network error.")
                    }
                    else -> {
                        Log.d(TAG, "Couldn't get credential from result." + " (${e.localizedMessage})")
                    }
                }
            }
        } else {
            Log.d(TAG, "result not OK")
        }
    }

    fun signedIn(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity.startActivity(intent)
        activity.finish()
    }

    fun signOut(activity: Activity, auth: FirebaseAuth) {
        auth.signOut()
        signedOut(activity)
    }

    private fun signedOut(activity: Activity) {
        val intent = Intent(activity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity.startActivity(intent)
        activity.finish()
    }
}