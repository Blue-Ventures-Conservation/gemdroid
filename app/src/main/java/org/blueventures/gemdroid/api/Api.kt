package org.blueventures.gemdroid.api

import com.github.zibnix.droidbones.api.BaseApi
import com.github.zibnix.droidbones.api.TokenInterceptor
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.data.analysis.ImageryExports
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.analysis.classification.ClassificationExports
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.data.analysis.classification.ClassificationReady
import org.blueventures.gemdroid.data.analysis.classification.ClassificationReadyResponse
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
    private const val backendBaseUrl = "https://gembackend-764487343690.africa-south1.run.app/"

    private fun backend(timeout: Long): Service {
        val pair = BaseApi.authRetrofit(backendBaseUrl, timeout, BuildConfig.DEBUG)
        return Service(pair.second.create(Backend::class.java), pair.first)
    }

    interface Backend {
        @POST("/get_imagery")
        suspend fun getVisualizeURLs(@Body roi: ROI): Result<VisualizeURLs>

        @POST("export_imagery")
        suspend fun exportLandsat(@Body roi: ROI): Result<ImageryExports>

        @POST("/upload_cra")
        suspend fun ingestCRA(@Body key: CRAKey): Result<CRAIngestRequested>

        @POST("/await_cra_upload")
        suspend fun awaitCRAIngestion(@Body name: UploadName): Result<Success>

        @POST("/classification_ready")
        suspend fun classificationReady(@Body roi: ClassificationReady): Result<ClassificationReadyResponse>

        @POST("/classification")
        suspend fun classification(@Body roi: ClassificationROI): Result<ClassificationURLs>

        @POST("export_classification")
        suspend fun exportClassification(@Body roi: ClassificationROI): Result<ClassificationExports>

        @POST("/box_chart")
        suspend fun boxChart(@Body roi: CraROI): Result<Map<String, Any>>

        @POST("/scatter_chart")
        suspend fun scatterChart(@Body roi: CraROI): Result<Map<String, Any>>

        @POST("/correlation_chart")
        suspend fun correlationChart(@Body roi: CraROI): Result<Map<String, Any>>

        @POST("/dynamics_ready")
        suspend fun dynamicsReady(@Body roi: DynamicsReady): Result<DynamicsReadyResponse>

        @POST("/dynamics")
        suspend fun dynamics(@Body roi: DynamicsROI): Result<DynamicsURLs>

        @POST("/export_dynamics")
        suspend fun exportDynamics(@Body roi: DynamicsROI): Result<DynamicsExports>

        @POST("task_status")
        suspend fun tasksResults(@Body tasks: Tasks): Result<TasksResults>
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