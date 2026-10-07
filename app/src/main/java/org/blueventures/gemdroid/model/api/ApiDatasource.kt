package org.blueventures.gemdroid.model.api

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.api.Token
import org.blueventures.gemdroid.data.Serializer
import java.io.File
import kotlin.coroutines.resume

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

open class ApiDatasource(
    private val api: Api.Service = Api.Service.instance(),
    private val auth: FirebaseAuth = Firebase.auth,
    private val storage: FirebaseStorage = Firebase.storage,
) {
    suspend fun getIdToken() = Token.get(auth, api)

    suspend fun uriFromStorage(path: String): Result<Uri> = suspendCancellableCoroutine { cont ->
        storage.reference.child(path).downloadUrl.complete(cont)
    }

    suspend fun <I, O> getRemote(body: I, call: suspend (Api.Service, I) -> ApiResult<O>) = call(api, body)
    fun <T, S : Serializer<T>> loadFile(file: File, serializer: S) = serializer.fromFile(file)
    fun <T, S : Serializer<T>> saveFile(file: File, data: T, serializer: S) = serializer.toFile(file, data)
    fun deleteFile(file: File) = FileService.deleteFile(file)
    fun renameFile(file: File, newName: String) = FileService.renameFile(file, newName)
    fun zipFile(files: List<File>, out: File) = FileService.zip(files, out)
}

fun <T> Task<T>.complete(cont: CancellableContinuation<Result<T>>) = complete(cont) { it }

fun <T, V> Task<T>.storage(cont: CancellableContinuation<Result<V>>, onSuccess: (T) -> V?) = complete(cont, {
    NoStack(R.string.could_not_reach_storage) }, onSuccess)
fun <T> Task<T>.storage(cont: CancellableContinuation<Result<Unit>>) = complete(cont, {
    NoStack(R.string.could_not_reach_storage) }, {})

fun <T, V> Task<T>.complete(cont: CancellableContinuation<Result<V>>, onFailure: (Exception?) -> Throwable? = {null}, onSuccess: (T) -> V?) {
    addOnCompleteListener { t ->
        if (t.isSuccessful) {
            onSuccess(t.result)?.let { proc ->
                cont.resume(Result.success(proc))
            }
        } else {
            cont.resume(Result.failure(onFailure(t.exception) ?: NoStack()))
        }
    }
}