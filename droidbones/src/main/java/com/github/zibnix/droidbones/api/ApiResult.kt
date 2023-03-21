package com.github.zibnix.droidbones.api

sealed class ApiResult<T> (val data: T?, val message: String?) {
    class Success<T>(data: T) : ApiResult<T>(data, null)
    class Error<T>(message: String?) : ApiResult<T>(null, message)
}

fun <T> apiResultCheck(r1: ApiResult<T>?, r2: ApiResult<T>?) = apiResultCheck(r1) ?: apiResultCheck(r2)
fun <T> apiResultCheck(result: ApiResult<T>?) = if (result != null && result is ApiResult.Error) Throwable(result.message!!) else null