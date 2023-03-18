package org.blueventures.gemdroid.model.analysis.separability

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.Success
import org.blueventures.gemdroid.data.UploadName
import org.blueventures.gemdroid.model.analysis.cra.CraDatasource.Companion.crasDir
import org.blueventures.gemdroid.model.analysis.cra.CraDatasource.Companion.crasFile
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class SeparabilityDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api = api) {
    fun loadCRAs(roiDir: File, callback: (Result<CRA>) -> Unit)  = runBlocking {
        val result = CRA.fromFile(File(File(roiDir, crasDir), crasFile))
        if (result.isFailure) {
            callback(result)
            return@runBlocking
        }

        val cra = result.getOrNull()!!

        val successes = AtomicInteger()
        val failures = AtomicInteger()
        var contSuccess: Success? = null
        var histSuccess: Success? = null

        val handleResult: ((Success?) -> Unit) -> (ApiResult<Success>) -> Unit = { setter -> { result ->
            if (result is ApiResult.Success) {
                setter(result.data)
                if (successes.addAndGet(1) == 2) {
                    if (contSuccess?.success == true && histSuccess?.success == true) {
                        callback(Result.success(cra))
                    } else {
                        callback(Result.failure(Throwable("Could not ingest CRAs into Earth Engine.")))
                    }
                }
            } else if (failures.addAndGet(1) == 1) {
                callback(Result.failure(Throwable(result.message!!)))
            }
        }}

        val cont = cra.contemporaryCRA
        val hist = cra.historicalCRA

        if (hist == null || cont == hist) {
            successes.addAndGet(1)
        } else {
             launch { handleResult { histSuccess = it }(awaitCRAIngestion(hist.tableUploadOperationName, hist.shapefileStorageKey)) }
        }
        launch { handleResult { contSuccess = it }(awaitCRAIngestion(cont.tableUploadOperationName, cont.shapefileStorageKey)) }
    }

    private suspend fun awaitCRAIngestion(name: String, key: String) = api.awaitCRAUpload(UploadName(name, key))

    companion object {
        const val separabilityDir = "separability"
        const val chotSamplesFile = "chot_samples.json"
        const val clotSamplesFile = "clot_samples.json"
        const val hhotSamplesFile = "hhot_samples.json"
        const val hlotSamplesFile = "hlot_samples.json"
        const val chotCorrelationFile = "chot_corr.json"
        const val clotCorrelationFile = "clot_corr.json"
        const val hhotCorrelationFile = "hhot_corr.json"
        const val hlotCorrelationFile = "hlot_corr.json"
        const val chotLSBandsSeparationFile = "chot_ls_separation.json"
        const val clotLSBandsSeparationFile = "clot_ls_separation.json"
        const val hhotLSBandsSeparationFile = "hhot_ls_separation.json"
        const val hlotLSBandsSeparationFile = "hlot_ls_separation.json"
        const val chotIndicesSeparationFile = "chot_indices_separation.json"
        const val clotIndicesSeparationFile = "clot_indices_separation.json"
        const val hhotIndicesSeparationFile = "hhot_indices_separation.json"
        const val hlotIndicesSeparationFile = "hlot_indices_separation.json"
    }
}