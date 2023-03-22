package org.blueventures.gemdroid.api

import com.google.firebase.auth.FirebaseAuth
import org.blueventures.gemdroid.model.SignIn
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Token {
    suspend fun get(auth: FirebaseAuth, api: Api.Service): Result<Unit> = suspendCoroutine { cont ->
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