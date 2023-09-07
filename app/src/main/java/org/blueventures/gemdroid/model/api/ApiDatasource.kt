package org.blueventures.gemdroid.model.api

import android.net.Uri
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.FileService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.api.Token
import org.blueventures.gemdroid.data.Serializer
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    suspend fun <I, O> getRemote(req: I, call: suspend (Api.Service, I) -> ApiResult<O>) = call(api, req)
    fun <T, S : Serializer<T>> loadFile(file: File, serializer: S) = serializer.fromFile(file)
    fun <T, S : Serializer<T>> saveFile(file: File, data: T, serializer: S): Result<Unit> {
        return serializer.toFile(file, data)
    }
    fun deleteFile(file: File) = FileService.deleteFile(file)
}