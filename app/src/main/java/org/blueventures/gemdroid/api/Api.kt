package org.blueventures.gemdroid.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.BaseApi
import com.github.zibnix.droidbones.api.TokenInterceptor
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.CRAKey
import org.blueventures.gemdroid.data.CRAUploadResult
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
        suspend fun uploadCRA(@Body key: CRAKey): ApiResult<CRAUploadResult>

        @POST("/await_cra_upload")
        suspend fun awaitCRAUpload(@Body name: UploadName): ApiResult<Success>
    }

    class Service(private val backend: Backend, private val auth: TokenInterceptor): Backend {
        fun setToken(token: String) { auth.token = token }
        override suspend fun getBuffers(roi: ROI) = backend.getBuffers(roi)
        override suspend fun getVisualizeURLs(roi: ROI) = backend.getVisualizeURLs(roi)
        override suspend fun uploadCRA(key: CRAKey) = backend.uploadCRA(key)
        override suspend fun awaitCRAUpload(name: UploadName) = backend.awaitCRAUpload(name)

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