package com.github.zibnix.droidbones.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object BaseApi {
    /**
     * timeout in seconds
     */
    fun resultRetrofit(baseUrl: String, timeout: Long, debug: Boolean = true, vararg factories: CallAdapter.Factory): Retrofit {
        return retrofit(baseUrl, timeout, debug, ResultCallAdapterFactory.create(), *factories)
    }

    /**
     * timeout in seconds
     */
    fun retrofit(baseUrl: String, timeout: Long, debug: Boolean = true, vararg factories: CallAdapter.Factory): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client(timeout, debug))
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            ))

        for (factory in factories) {
            builder.addCallAdapterFactory(factory)
        }

        return builder.build()
    }

    private fun client(timeout: Long, debug: Boolean): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
        if (debug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        return builder.build()
    }

    fun <T : Any> handleResponse(execute: () -> Response<T>): ApiResult<T> {
        return try {
            val response = execute()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("$response.code(): $response.message()")
            }
        } catch (e: HttpException) {
            ApiResult.Error("$e.code: $e.message()")
        } catch (e: Throwable) {
            ApiResult.Error(e.message)
        }
    }
}