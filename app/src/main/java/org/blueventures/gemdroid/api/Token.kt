package org.blueventures.gemdroid.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import org.blueventures.gemdroid.model.SignIn
import kotlin.coroutines.resume

object Token {
    suspend fun get(auth: FirebaseAuth, api: Api.Service): Result<Unit> = suspendCancellableCoroutine { cont ->
        auth.currentUser?.let { user ->
            user.getIdToken(false).addOnSuccessListener { result ->
                api.setToken(result.token!!)
                cont.resume(Result.success(Unit))
            }.addOnFailureListener { ex ->
                cont.resume(Result.failure(ex))
            }
        } ?: run {
            cont.resume(Result.failure(SignIn.not))
        }
    }
}