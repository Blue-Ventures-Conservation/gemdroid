package com.github.zibnix.droidbones.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Must set token before making requests that include this interceptor.
 */
class TokenInterceptor: Interceptor {
    lateinit var token: String

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.header("Authorization", "Bearer $token")
        return chain.proceed(builder.build())
    }
}