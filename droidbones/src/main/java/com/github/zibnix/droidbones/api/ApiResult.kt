package com.github.zibnix.droidbones.api

sealed class ApiResult<T> (val data: T?, val message: String?) {
    class Success<T>(data: T) : ApiResult<T>(data, null)
    class Error<T>(message: String?) : ApiResult<T>(null, message)
}