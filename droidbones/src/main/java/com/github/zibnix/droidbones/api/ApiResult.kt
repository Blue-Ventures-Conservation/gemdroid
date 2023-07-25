package com.github.zibnix.droidbones.api

import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.R

sealed class ApiResult<T> (val data: T?, val code: Int?, val message: String?) {
    class Success<T>(data: T) : ApiResult<T>(data, null, null)
    class Error<T>(code: Int?, message: String?) : ApiResult<T>(null, code, message)
}

fun <T> apiResultCheck(r1: ApiResult<T>?, r2: ApiResult<T>?) = apiResultCheck(r1) ?: apiResultCheck(r2)
fun <T> apiResultCheck(result: ApiResult<T>?) = if (result != null && result is ApiResult.Error) NoStack(R.string.network_error_try_again) else null