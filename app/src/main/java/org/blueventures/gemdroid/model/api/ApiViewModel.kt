package org.blueventures.gemdroid.model.api

import android.net.Uri
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.analysis.TasksResults

open class ApiViewModel(private val repo: ApiRepository = ApiRepository()): BaseViewModel() {
    fun <T> apiWithToken(jobserver: (Job) -> Unit, flow: Flow<ApiResult<T>>, callback: (ApiResult<T>) -> Unit) {
        withToken(jobserver, ::apiErr, flow, callback)
    }

    fun <T> resultWithToken(jobserver: (Job) -> Unit, flow: Flow<Result<T>>, callback: (Result<T>) -> Unit) {
        withToken(jobserver, ::resErr, flow, callback)
    }

    fun <T> withToken(jobserver: (Job) -> Unit, errFun: (Throwable) -> T,  flow: Flow<T>, callback: (T) -> Unit) {
        jobserver(scoped {
            repo.getIdToken().collect { result ->
                when {
                    result.isSuccess -> flow.collect(callback)
                    else -> callback(errFun(result.exceptionOrNull()!!))
                }
            }
        })
    }

    fun background(work: () -> Unit) = scoped { repo.background(work).collect() }

    fun getTasksResults(jobserver: (Job) -> Unit, names: List<String>, callback: (ApiResult<TasksResults>) -> Unit) = apiWithToken(jobserver, repo.getTasksResults(*names.toTypedArray()), callback)

    fun uriFromStorage(jobserver: (Job) -> Unit, path: String, callback: (Result<Uri>) -> Unit) = resultWithToken(jobserver, repo.uriFromStorage(path), callback)

    companion object {
        fun <T> apiErr(err: Throwable) = ApiResult.Error<T>(null, err.message)
        fun <T> resErr(err: Throwable) = Result.failure<T>(err)
    }
}