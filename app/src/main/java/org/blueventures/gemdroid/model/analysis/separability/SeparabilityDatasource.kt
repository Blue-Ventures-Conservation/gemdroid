package org.blueventures.gemdroid.model.analysis.separability

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.api.apiResultCheck
import com.github.zibnix.droidbones.mvvm.FileService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.CraROI
import org.blueventures.gemdroid.data.JSONMap
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.Success
import org.blueventures.gemdroid.data.UploadName
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource
import org.blueventures.gemdroid.model.analysis.cra.CraDatasource.Companion.crasDir
import org.blueventures.gemdroid.model.analysis.cra.CraDatasource.Companion.crasFile
import org.blueventures.gemdroid.model.analysis.separability.Paths.separabilityDir
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.resultCheck
import java.io.File

class SeparabilityDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api = api) {
    suspend fun loadCRAs(roiDir: File): Result<Pair<CRA, Throwable?>> {
        val result = CRA.fromFile(File(File(roiDir, crasDir), crasFile))
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }

        val cra = result.getOrNull()!!
        val cont = cra.contemporaryCRA
        val hist = cra.historicalCRA

        var contResult: ApiResult<Success>? = null
        var histResult: ApiResult<Success>? = null

        // returns when all children coroutines are complete
        coroutineScope {
            if (hist != null && cont != hist) {
                launch { histResult = awaitCRAIngestion(hist.tableUploadOperationName, hist.shapefileStorageKey) }
            }
            launch { contResult = awaitCRAIngestion(cont.tableUploadOperationName, cont.shapefileStorageKey) }
        }

        val err = apiResultCheck(contResult, histResult)
        if (err != null) {
            return Result.success(Pair(cra, err))
        }

        if (!contResult!!.data!!.success || !histResult!!.data!!.success) {
            return Result.success(Pair(cra, Throwable()))
        }

        return Result.success(Pair(cra, null))
    }

    private suspend fun awaitCRAIngestion(name: String, key: String) = api.awaitCRAUpload(UploadName(name, key))

    suspend fun getSeparation(tp: TimePeriod, craROI: CraROI) = tp.separation(api, craROI)
    fun saveSeparationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = mkdir(roiDir) { JSONMap.toFile(File(File(roiDir, separabilityDir), tp.separationFile), data) }
    fun loadSeparationFile(roiDir: File, tp: TimePeriod) = JSONMap.fromFile(File(File(roiDir, separabilityDir), tp.separationFile))

    suspend fun getScatter(tp: TimePeriod, craROI: CraROI) = tp.scatter(api, craROI)
    fun saveScatterFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = mkdir(roiDir) { JSONMap.toFile(File(File(roiDir, separabilityDir), tp.scatterFile), data) }
    fun loadScatterFile(roiDir: File, tp: TimePeriod) = JSONMap.fromFile(File(File(roiDir, separabilityDir), tp.scatterFile))

    suspend fun getCorrelation(tp: TimePeriod, craROI: CraROI) = tp.correlation(api, craROI)
    fun saveCorrelationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = mkdir(roiDir) { JSONMap.toFile(File(File(roiDir, separabilityDir), tp.correlationFile), data) }
    fun loadCorrelationFile(roiDir: File, tp: TimePeriod) = JSONMap.fromFile(File(File(roiDir, separabilityDir), tp.correlationFile))

    fun getROI(roiDir: File) = ROI.fromFile(File(roiDir, AnalysisDatasource.roiFilename))

    private fun mkdir(roiDir: File, func: () -> Result<Unit>): Result<Unit> {
        val err = resultCheck(FileService.createDir(roiDir, separabilityDir))
        if (err != null) return Result.failure(err)
        return func()
    }
}

object Paths {
    const val separabilityDir = "separability"
    const val chotSeparationFile = "chot_separation.json"
    const val clotSeparationFile = "clot_separation.json"
    const val hhotSeparationFile = "hhot_separation.json"
    const val hlotSeparationFile = "hlot_separation.json"
    const val chotScatterFile = "chot_scatter.json"
    const val clotScatterFile = "clot_scatter.json"
    const val hhotScatterFile = "hhot_scatter.json"
    const val hlotScatterFile = "hlot_scatter.json"
    const val chotCorrelationFile = "chot_corr.json"
    const val clotCorrelationFile = "clot_corr.json"
    const val hhotCorrelationFile = "hhot_corr.json"
    const val hlotCorrelationFile = "hlot_corr.json"
}

sealed interface TimePeriod {
    val apiVal: Int // don't modify these values without coordinating with the backend
    val separationFile: String
    val scatterFile: String
    val correlationFile: String
    suspend fun separation(api: Api.Service, craROI: CraROI): ApiResult<Map<String, Any>>
    suspend fun scatter(api: Api.Service, craROI: CraROI): ApiResult<Map<String, Any>>
    suspend fun correlation(api: Api.Service, craROI: CraROI): ApiResult<Map<String, Any>>
}
object ContemporaryHighTide: TimePeriod {
    override val apiVal: Int = 1
    override val separationFile = Paths.chotSeparationFile
    override val scatterFile = Paths.chotScatterFile
    override val correlationFile = Paths.chotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}
object ContemporaryLowTide: TimePeriod {
    override val apiVal: Int = 2
    override val separationFile = Paths.clotSeparationFile
    override val scatterFile = Paths.clotScatterFile
    override val correlationFile = Paths.clotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}
object HistoricalHighTide: TimePeriod {
    override val apiVal: Int = 3
    override val separationFile = Paths.hhotSeparationFile
    override val scatterFile = Paths.hhotScatterFile
    override val correlationFile = Paths.hhotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}
object HistoricalLowTide: TimePeriod {
    override val apiVal: Int = 4
    override val separationFile = Paths.hlotSeparationFile
    override val scatterFile = Paths.hlotScatterFile
    override val correlationFile = Paths.hlotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}