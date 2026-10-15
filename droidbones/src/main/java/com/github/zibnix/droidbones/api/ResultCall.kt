package com.github.zibnix.droidbones.api

import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.R
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class ResultCall<T>(private val proxy: Call<T>): Call<Result<T>> {
    override fun enqueue(callback: Callback<Result<T>>) {
        proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@ResultCall, Response.success(handleApi(response)))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@ResultCall, Response.success(Result.failure(t)))
            }
        })
    }

    override fun execute(): Response<Result<T>> = throw NotImplementedError()
    override fun clone(): Call<Result<T>> = ResultCall(proxy.clone())
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() { proxy.cancel() }

    companion object {
        fun <T> handleApi(response: Response<T>): Result<T> {
            return try {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Throwable(response.message()))
                }
            } catch (_: HttpException) {
                Result.failure(NoStack(R.string.network_error_try_again))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }
}