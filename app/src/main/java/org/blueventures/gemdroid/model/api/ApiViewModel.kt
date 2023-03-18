package org.blueventures.gemdroid.model.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

open class ApiViewModel(private val repo: ApiRepository = ApiRepository()): BaseViewModel() {
    fun <T> withToken(jobserver: (Job) -> Unit, call: () -> Flow<ApiResult<T>>, callback: (ApiResult<T>) -> Unit) {
        repo.getIdToken { result ->
            when {
                result.isSuccess -> jobserver(scoped { call().collect(callback) })
                else -> callback(ApiResult.Error(result.exceptionOrNull()!!.message))
            }
        }
    }

    fun <T> withToken(call: () -> Unit, callback: (Result<T>) -> Unit) {
        repo.getIdToken { result ->
            when {
                result.isSuccess -> call()
                else -> callback(Result.failure(result.exceptionOrNull()!!))
            }
        }
    }
}