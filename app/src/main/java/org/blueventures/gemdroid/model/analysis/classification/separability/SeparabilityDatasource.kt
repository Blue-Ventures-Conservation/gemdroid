package org.blueventures.gemdroid.model.analysis.classification.separability

import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.analysis.cra.CraROI
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File

class SeparabilityDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    suspend fun getSeparation(tp: TimePeriod, craROI: CraROI) = tp.separation(api, craROI)
    fun saveSeparationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = JSONMap.toFile(File(sepDir(roiDir), tp.separationFile), data)
    fun loadSeparationFile(roiDir: File, tp: TimePeriod) = JSONMap.fromFile(File(sepDir(roiDir), tp.separationFile))

    suspend fun getScatter(tp: TimePeriod, craROI: CraROI) = tp.scatter(api, craROI)
    fun saveScatterFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = JSONMap.toFile(File(sepDir(roiDir), tp.scatterFile), data)
    fun loadScatterFile(roiDir: File, tp: TimePeriod) = JSONMap.fromFile(File(sepDir(roiDir), tp.scatterFile))

    suspend fun getCorrelation(tp: TimePeriod, craROI: CraROI) = tp.correlation(api, craROI)
    fun saveCorrelationFile(roiDir: File, tp: TimePeriod, data: Map<String, Any>) = JSONMap.toFile(File(sepDir(roiDir), tp.correlationFile), data)
    fun loadCorrelationFile(roiDir: File, tp: TimePeriod) = JSONMap.fromFile(File(sepDir(roiDir), tp.correlationFile))

    private fun sepDir(roiDir: File) = File(roiDir, separabilityDir)

    companion object {
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
    override val separationFile = SeparabilityDatasource.chotSeparationFile
    override val scatterFile = SeparabilityDatasource.chotScatterFile
    override val correlationFile = SeparabilityDatasource.chotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}
object ContemporaryLowTide: TimePeriod {
    override val apiVal: Int = 2
    override val separationFile = SeparabilityDatasource.clotSeparationFile
    override val scatterFile = SeparabilityDatasource.clotScatterFile
    override val correlationFile = SeparabilityDatasource.clotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}
object HistoricalHighTide: TimePeriod {
    override val apiVal: Int = 3
    override val separationFile = SeparabilityDatasource.hhotSeparationFile
    override val scatterFile = SeparabilityDatasource.hhotScatterFile
    override val correlationFile = SeparabilityDatasource.hhotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}
object HistoricalLowTide: TimePeriod {
    override val apiVal: Int = 4
    override val separationFile = SeparabilityDatasource.hlotSeparationFile
    override val scatterFile = SeparabilityDatasource.hlotScatterFile
    override val correlationFile = SeparabilityDatasource.hlotCorrelationFile
    override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
    override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
    override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
}