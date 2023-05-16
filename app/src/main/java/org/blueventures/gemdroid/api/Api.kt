package org.blueventures.gemdroid.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.BaseApi
import com.github.zibnix.droidbones.api.TokenInterceptor
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.CRAIngestRequested
import org.blueventures.gemdroid.data.CRAKey
import org.blueventures.gemdroid.data.CraROI
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.Success
import org.blueventures.gemdroid.data.UploadName
import org.blueventures.gemdroid.data.VisualizeURLs
import retrofit2.http.Body
import retrofit2.http.POST

object Api {
    private const val backendBaseUrl = "http://192.168.0.54:8080/"

    private fun backend(timeout: Long): Service {
        val pair = BaseApi.authResultRetrofit(backendBaseUrl, timeout, BuildConfig.DEBUG)
        return Service(pair.second.create(Backend::class.java), pair.first)
    }

    interface Backend {
        @POST("/area_chart")
        suspend fun getBuffers(@Body roi: ROI): ApiResult<Buffers>

        @POST("/ls_imagery")
        suspend fun getVisualizeURLs(@Body roi: ROI): ApiResult<VisualizeURLs>

        @POST("/upload_cra")
        suspend fun ingestCRA(@Body key: CRAKey): ApiResult<CRAIngestRequested>

        @POST("/await_cra_upload")
        suspend fun awaitCRAUpload(@Body name: UploadName): ApiResult<Success>

        @POST("/box_chart")
        suspend fun boxChart(@Body craROI: CraROI): ApiResult<Map<String, Any>>

        @POST("/scatter_chart")
        suspend fun scatterChart(@Body craROI: CraROI): ApiResult<Map<String, Any>>

        @POST("/correlation_chart")
        suspend fun correlationChart(@Body craROI: CraROI): ApiResult<Map<String, Any>>
    }

    class Service(private val backend: Backend, private val tokenHolder: TokenInterceptor): Backend by backend {
        fun setToken(token: String) { tokenHolder.token = token }

        companion object {
            private var timeout: Long = 600

            private val service: Service by lazy {
                backend(timeout)
            }

            fun instance(timeout: Long = 600): Service {
                Companion.timeout = timeout
                return service
            }
        }
    }
}