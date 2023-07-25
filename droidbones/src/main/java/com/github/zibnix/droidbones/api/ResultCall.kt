package com.github.zibnix.droidbones.api

import com.github.zibnix.droidbones.api.BaseApi.handleResponse
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultCall<T : Any>(private val proxy: Call<T>) : Call<ApiResult<T>> {

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@ResultCall, Response.success(handleResponse { response }))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@ResultCall, Response.success(ApiResult.Error(null, t.message)))
            }
        })
    }

    override fun execute(): Response<ApiResult<T>> = throw NotImplementedError()
    override fun clone(): Call<ApiResult<T>> = ResultCall(proxy.clone())
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() { proxy.cancel() }
}