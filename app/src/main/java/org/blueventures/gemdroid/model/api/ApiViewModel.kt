package org.blueventures.gemdroid.model.api

import android.net.Uri
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Serializer
import org.blueventures.gemdroid.data.analysis.TasksResults
import java.io.File

open class ApiViewModel(private val repo: ApiRepository = ApiRepository()): BaseViewModel() {
    fun <T> apiWithToken(prev: Job?, flow: Flow<ApiResult<T>>, callback: (ApiResult<T>) -> Unit) = withToken(prev, ::apiErr, flow, callback)

    fun <T> resultWithToken(prev: Job?, flow: Flow<Result<T>>, callback: (Result<T>) -> Unit) = withToken(prev, ::resErr, flow, callback)

    fun <T> withToken(prev: Job?, errFun: (Throwable) -> T,  flow: Flow<T>, callback: (T) -> Unit): Job {
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

    fun background(work: () -> Unit) = scoped { repo.background(work).collect() }

    fun uriFromStorage(prev: Job?, path: String, callback: (Result<Uri>) -> Unit) = resultWithToken(prev, repo.uriFromStorage(path), callback)

    fun <I, O> getRemote(prev: Job?, req: I, callback: (ApiResult<O>) -> Unit, call: suspend (Api.Service, I) -> ApiResult<O>) = apiWithToken(prev, repo.getRemote(req, call), callback)
    fun <T, S : Serializer<T>> loadFile(file: File, serializer: S, callback: (Result<T>) -> Unit) = scoped { repo.loadFile(file, serializer).collect(callback) }
    fun <T, S : Serializer<T>> saveFile(file: File, data: T, serializer: S, callback: (Result<Unit>) -> Unit = {}) = scoped { repo.saveFile(file, data, serializer).collect(callback) }
    fun deleteFile(file: File, callback: (Result<Unit>) -> Unit = {}) = scoped { repo.deleteFile(file).collect(callback) }

    fun saveResults(file: File, results: TasksResults) = saveFile(file, results, TasksResults.Companion)
    fun loadResults(file: File, callback: (Result<TasksResults>) -> Unit) = loadFile(file, TasksResults.Companion, callback)

    companion object {
        fun <T> apiErr(err: Throwable) = ApiResult.Error<T>(null, err.message)
        fun <T> resErr(err: Throwable) = Result.failure<T>(err)
    }
}