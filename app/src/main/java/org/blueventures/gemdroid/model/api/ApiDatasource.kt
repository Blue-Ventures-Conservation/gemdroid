package org.blueventures.gemdroid.model.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.api.Token

open class ApiDatasource(
    private val auth: FirebaseAuth = Firebase.auth,
    private val api: Api.Service = Api.Service.instance(),
) {
    suspend fun getIdToken(): Result<Unit> = Token.get(auth, api)
}