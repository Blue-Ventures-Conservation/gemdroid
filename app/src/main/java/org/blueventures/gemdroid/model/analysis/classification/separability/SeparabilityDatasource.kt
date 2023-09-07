package org.blueventures.gemdroid.model.analysis.classification.separability

import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.analysis.cra.CraROI
import org.blueventures.gemdroid.model.api.ApiDatasource
import java.io.File

class SeparabilityDatasource(
    api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    companion object {
        private const val separabilityDir = "separability"
        private const val chotSeparationFile = "chot_separation.json"
        private const val clotSeparationFile = "clot_separation.json"
        private const val hhotSeparationFile = "hhot_separation.json"
        private const val hlotSeparationFile = "hlot_separation.json"
        private const val chotScatterFile = "chot_scatter.json"
        private const val clotScatterFile = "clot_scatter.json"
        private const val hhotScatterFile = "hhot_scatter.json"
        private const val hlotScatterFile = "hlot_scatter.json"
        private const val chotCorrelationFile = "chot_corr.json"
        private const val clotCorrelationFile = "clot_corr.json"
        private const val hhotCorrelationFile = "hhot_corr.json"
        private const val hlotCorrelationFile = "hlot_corr.json"

        fun sepDir(roiDir: File) = File(roiDir, separabilityDir)
        fun separationFile(roiDir: File, tp: TimePeriod) = File(sepDir(roiDir), tp.separationFile)
        fun scatterFile(roiDir: File, tp: TimePeriod) = File(sepDir(roiDir), tp.scatterFile)
        fun correlationFile(roiDir: File, tp: TimePeriod) = File(sepDir(roiDir), tp.correlationFile)
    }

    sealed interface TimePeriod {
        val apiVal: Int // don't modify these values without coordinating with the backend
        val separationFile: String
        val scatterFile: String
        val correlationFile: String
        suspend fun separation(api: Api.Service, craROI: CraROI): ApiResult<Map<String, Any>>
        suspend fun scatter(api: Api.Service, craROI: CraROI): ApiResult<Map<String, Any>>
        suspend fun correlation(api: Api.Service, craROI: CraROI): ApiResult<Map<String, Any>>

        fun contemporary() = this is ContemporaryHighTide || this is ContemporaryLowTide
        fun historical() = !contemporary()
    }
    object ContemporaryHighTide: TimePeriod {
        override val apiVal: Int = 1
        override val separationFile = chotSeparationFile
        override val scatterFile = chotScatterFile
        override val correlationFile = chotCorrelationFile
        override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
        override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
        override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
    }
    object ContemporaryLowTide: TimePeriod {
        override val apiVal: Int = 2
        override val separationFile = clotSeparationFile
        override val scatterFile = clotScatterFile
        override val correlationFile = clotCorrelationFile
        override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
        override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
        override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
    }
    object HistoricalHighTide: TimePeriod {
        override val apiVal: Int = 3
        override val separationFile = hhotSeparationFile
        override val scatterFile = hhotScatterFile
        override val correlationFile = hhotCorrelationFile
        override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
        override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
        override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
    }
    object HistoricalLowTide: TimePeriod {
        override val apiVal: Int = 4
        override val separationFile = hlotSeparationFile
        override val scatterFile = hlotScatterFile
        override val correlationFile = hlotCorrelationFile
        override suspend fun separation(api: Api.Service, craROI: CraROI) = api.boxChart(craROI.copy(timePeriod = apiVal))
        override suspend fun scatter(api: Api.Service, craROI: CraROI) = api.scatterChart(craROI.copy(timePeriod = apiVal))
        override suspend fun correlation(api: Api.Service, craROI: CraROI) = api.correlationChart(craROI.copy(timePeriod = apiVal))
    }
}