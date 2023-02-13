package org.blueventures.gemdroid.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.BaseApi
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import retrofit2.http.Body
import retrofit2.http.POST

object Api {
    private const val backendBaseUrl = "http://192.168.10.174:8080/"

    private fun backend(timeout: Long): BackendService {
        return BaseApi.resultRetrofit(backendBaseUrl, timeout, BuildConfig.DEBUG).create(BackendService::class.java)
    }

    interface BackendService {
        @POST("/area_chart")
        suspend fun getBuffers(@Body roi: ROI.Data): ApiResult<Buffers.Data>

        companion object {
            private var timeout: Long = 600

            private val service: BackendService by lazy {
                backend(timeout)
            }

            fun instance(timeout: Long = 600): BackendService {
                Companion.timeout = timeout
                return service
            }
        }
    }
}