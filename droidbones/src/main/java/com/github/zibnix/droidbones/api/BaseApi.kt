package com.github.zibnix.droidbones.api

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object BaseApi {
    val moshi: Moshi = Moshi.Builder().build()

    /**
     * timeout in seconds
     */
    fun authRetrofit(baseUrl: String, timeout: Long, debug: Boolean = true, json: Boolean = true): Pair<TokenInterceptor, Retrofit> {
        val builder = client(timeout, debug, json)
        val tokenIntercept = TokenInterceptor()
        builder.addInterceptor(tokenIntercept)
        return Pair(tokenIntercept, retrofit(baseUrl, builder))
    }

    /**
     * timeout in seconds
     */
    fun retrofit(baseUrl: String, httpBuilder: OkHttpClient.Builder): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ResultCallAdapterFactory.create())

        return builder.build()
    }

    private fun client(timeout: Long, debug: Boolean, json: Boolean): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
        if (json) {
            builder.addInterceptor { chain ->
                val requestBuilder: Request.Builder = chain.request().newBuilder()
                requestBuilder.header("Content-Type", "application/json")
                requestBuilder.header("Accept", "application/json")
                chain.proceed(requestBuilder.build())
            }
        }
        if (debug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        return builder
    }
}