package com.github.zibnix.droidbones.api

sealed class ApiResult<T : Any> (val data: T?, val message: String?) {
    class Success<T : Any>(data: T) : ApiResult<T>(data, null)
    class Error<T : Any>(message: String?) : ApiResult<T>(null, message)
}