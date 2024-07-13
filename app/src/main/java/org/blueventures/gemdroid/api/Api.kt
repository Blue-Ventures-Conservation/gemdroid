package org.blueventures.gemdroid.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.BaseApi
import com.github.zibnix.droidbones.api.TokenInterceptor
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.data.analysis.Buffers
import org.blueventures.gemdroid.data.analysis.ImageryExports
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.analysis.classification.ClassificationExports
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.cra.CRAIngestRequested
import org.blueventures.gemdroid.data.analysis.cra.CRAKey
import org.blueventures.gemdroid.data.analysis.cra.CraROI
import org.blueventures.gemdroid.data.analysis.cra.Success
import org.blueventures.gemdroid.data.analysis.cra.UploadName
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsExports
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsROI
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsReady
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsReadyResponse
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.data.roi.ROI
import retrofit2.http.Body
import retrofit2.http.POST

object Api {
    private const val backendBaseUrl = "https://gembackend-oxtfh6aefa-zf.a.run.app/"

    private fun backend(timeout: Long): Service {
        val pair = BaseApi.authResultRetrofit(backendBaseUrl, timeout, BuildConfig.DEBUG)
        return Service(pair.second.create(Backend::class.java), pair.first)
    }

    interface Backend {
        @POST("/area_chart")
        suspend fun getBuffers(@Body roi: ROI): ApiResult<Buffers>

        @POST("/ls_imagery")
        suspend fun getVisualizeURLs(@Body roi: ROI): ApiResult<VisualizeURLs>

        @POST("export_ls_imagery")
        suspend fun exportLandsat(@Body roi:ROI): ApiResult<ImageryExports>

        @POST("/upload_cra")
        suspend fun ingestCRA(@Body key: CRAKey): ApiResult<CRAIngestRequested>

        @POST("/await_cra_upload")
        suspend fun awaitCRAIngestion(@Body name: UploadName): ApiResult<Success>

        @POST("/classification")
        suspend fun classification(@Body roi: ClassificationROI): ApiResult<ClassificationURLs>

        @POST("export_classification")
        suspend fun exportClassification(@Body roi: ClassificationROI): ApiResult<ClassificationExports>

        @POST("/box_chart")
        suspend fun boxChart(@Body roi: CraROI): ApiResult<Map<String, Any>>

        @POST("/scatter_chart")
        suspend fun scatterChart(@Body roi: CraROI): ApiResult<Map<String, Any>>

        @POST("/correlation_chart")
        suspend fun correlationChart(@Body roi: CraROI): ApiResult<Map<String, Any>>

        @POST("/dynamics_ready")
        suspend fun dynamicsReady(@Body roi: DynamicsReady): ApiResult<DynamicsReadyResponse>

        @POST("/dynamics")
        suspend fun dynamics(@Body roi: DynamicsROI): ApiResult<DynamicsURLs>

        @POST("/export_dynamics")
        suspend fun exportDynamics(@Body roi: DynamicsROI): ApiResult<DynamicsExports>

        @POST("task_status")
        suspend fun tasksResults(@Body tasks: Tasks): ApiResult<TasksResults>
    }

    class Service(private val backend: Backend, private val tokenHolder: TokenInterceptor): Backend by backend {
        fun setToken(token: String) { tokenHolder.token = token }

        companion object {
            private var timeout: Long = 1200

            private val service: Service by lazy {
                backend(timeout)
            }

            fun instance(timeout: Long = 1200): Service {
                Companion.timeout = timeout
                return service
            }
        }
    }
}