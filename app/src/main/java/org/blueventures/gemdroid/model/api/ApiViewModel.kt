package org.blueventures.gemdroid.model.api

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.IOViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.data.analysis.TasksResults
import java.io.File

open class ApiViewModel(private val repo: ApiRepository = ApiRepository()): IOViewModel(repo) {
    fun <T> apiWithToken(prev: Job?, flow: Flow<ApiResult<T>>, callback: (ApiResult<T>) -> Unit) = withToken(prev, ::apiErr, flow, callback)

    fun <T> resultWithToken(prev: Job?, flow: Flow<Result<T>>, callback: (Result<T>) -> Unit) = withToken(prev, ::resErr, flow, callback)

    private fun <T> withToken(prev: Job?, errFun: (Throwable) -> T, flow: Flow<T>, callback: (T) -> Unit): Job {
        prev?.cancel()
        return scoped {
            repo.getIdToken().collect { result ->
                when {
                    result.isSuccess -> flow.collect(callback)
                    else -> callback(errFun(result.exceptionOrNull()!!))
                }
            }
        }
    }

    fun uriFromStorage(prev: Job?, path: String, callback: (Result<Uri>) -> Unit) = resultWithToken(prev, repo.uriFromStorage(path), callback)

    fun <I, O> getRemote(prev: Job?, body: I, callback: (ApiResult<O>) -> Unit, call: suspend (Api.Service, I) -> ApiResult<O>) = apiWithToken(prev, repo.getRemote(body, call), callback)
    fun <T, S : Serializer<T>> loadFile(file: File, serializer: S, callback: (Result<T>) -> Unit) = scoped { repo.loadFile(file, serializer).collect(callback) }
    fun <T, S : Serializer<T>> saveFile(file: File, data: T, serializer: S, callback: (Result<Unit>) -> Unit = {}) = scoped { repo.saveFile(file, data, serializer).collect(callback) }
    fun deleteFile(file: File, callback: (Result<Unit>) -> Unit = {}) = scoped { repo.deleteFile(file).collect(callback) }

    fun saveResults(file: File, results: TasksResults) = saveFile(file, results, TasksResults.Companion)
    fun loadResults(file: File, callback: (Result<TasksResults>) -> Unit) = loadFile(file, TasksResults.Companion, callback)

    fun <T> read(context: Context, key: Preferences.Key<T>, callback: (Result<T>) -> Unit) = scoped { repo.read(context, key).collect(callback) }
    fun <T> write(context: Context, key: Preferences.Key<T>, value: T, callback: (Preferences?) -> Unit = {}) = scoped { repo.write(context, key, value).collect(callback) }

    companion object {
        fun <T> apiErr(err: Throwable) = ApiResult.Error<T>(null, err.message)
        fun <T> resErr(err: Throwable) = Result.failure<T>(err)
    }
}