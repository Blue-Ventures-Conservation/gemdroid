package org.blueventures.gemdroid.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import org.blueventures.gemdroid.model.SignIn
import org.blueventures.gemdroid.model.api.complete
import kotlin.coroutines.resume

object Token {
    suspend fun get(auth: FirebaseAuth, api: Api.Service): Result<Unit> = suspendCancellableCoroutine { cont ->
        auth.currentUser?.let { user ->
            user.getIdToken(false).complete(cont) { result ->
                api.setToken(result.token!!)
            }
        } ?: run {
            cont.resume(Result.failure(SignIn.not))
        }
    }
}