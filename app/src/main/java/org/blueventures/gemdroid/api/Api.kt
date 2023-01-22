package org.blueventures.gemdroid.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.BaseApi
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.data.Todo
import org.blueventures.gemdroid.data.User
import retrofit2.http.GET
import retrofit2.http.Path

object Api {
    private const val placeholderBaseUrl = "https://jsonplaceholder.typicode.com/"

    private fun placeholder(timeout: Long): PlaceholderService {
        return BaseApi.resultRetrofit(placeholderBaseUrl, timeout, BuildConfig.DEBUG).create(PlaceholderService::class.java)
    }

    interface PlaceholderService {
        @GET("/todos/{id}")
        suspend fun getTodo(@Path(value = "id") todoId: Int): ApiResult<Todo>

        @GET("/users/{id}")
        suspend fun getUser(@Path(value = "id") userId: Int): ApiResult<User>

        companion object {
            private var timeout: Long = 20

            private val service: PlaceholderService by lazy {
                placeholder(timeout)
            }

            fun instance(timeout: Long = 20): PlaceholderService {
                Companion.timeout = timeout
                return service
            }
        }
    }
}