package org.blueventures.gemdroid.api

import com.google.firebase.auth.FirebaseAuth
import org.blueventures.gemdroid.model.SignIn

object Token {
    fun getIdToken(auth: FirebaseAuth, api: Api.Service, callback: (Result<Unit>) -> Unit) {
        auth.currentUser?.let { user ->
            user.getIdToken(false).addOnSuccessListener { result ->
                api.setToken(result.token!!)
                callback(Result.success(Unit))
            }.addOnFailureListener { ex ->
                callback(Result.failure(ex))
            }
        } ?: run {
            callback(Result.failure(SignIn.not))
        }
    }
}