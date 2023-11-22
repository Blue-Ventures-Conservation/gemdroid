package org.blueventures.gemdroid.model.api

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.api.Token
import org.blueventures.gemdroid.data.Serializer
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

open class ApiDatasource(
    private val api: Api.Service = Api.Service.instance(),
    private val auth: FirebaseAuth = Firebase.auth,
    private val storage: FirebaseStorage = Firebase.storage,
) {
    suspend fun getIdToken() = Token.get(auth, api)

    suspend fun uriFromStorage(path: String): Result<Uri> = suspendCoroutine { cont ->
        storage.reference.child(path).downloadUrl.addOnSuccessListener {
            cont.resume(Result.success(it))
        }.addOnFailureListener {
            cont.resume(Result.failure(it))
        }
    }

    suspend fun <I, O> getRemote(body: I, call: suspend (Api.Service, I) -> ApiResult<O>) = call(api, body)
    fun <T, S : Serializer<T>> loadFile(file: File, serializer: S) = serializer.fromFile(file)
    fun <T, S : Serializer<T>> saveFile(file: File, data: T, serializer: S) = serializer.toFile(file, data)
    fun deleteFile(file: File) = FileService.deleteFile(file)
}