package org.blueventures.gemdroid.model.api

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.api.Token
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class ApiDatasource(
    private val api: Api.Service = Api.Service.instance(),
    private val auth: FirebaseAuth = Firebase.auth,
    private val storage: FirebaseStorage = Firebase.storage,
) {
    suspend fun getIdToken() = Token.get(auth, api)
    fun loadTasksResults(file: File) = TasksResults.fromFile(file)
    fun saveTasksResults(file: File, results: TasksResults) = TasksResults.toFile(file, results)
    suspend fun getTasksResults(vararg names: String) = api.tasksResults(Tasks(listOf(*names)))

    suspend fun uriFromStorage(path: String): Result<Uri> = suspendCoroutine { cont ->
        storage.reference.child(path).downloadUrl.addOnSuccessListener {
            cont.resume(Result.success(it))
        }.addOnFailureListener {
            cont.resume(Result.failure(it))
        }
    }
}