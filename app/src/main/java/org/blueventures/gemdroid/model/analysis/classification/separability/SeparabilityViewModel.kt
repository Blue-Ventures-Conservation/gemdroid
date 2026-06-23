package org.blueventures.gemdroid.model.analysis.classification.separability

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.JSONMap
import org.blueventures.gemdroid.data.analysis.cra.CraROI
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.data.shp.RemoteCRAFileInfo
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.Companion.correlationFile
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.Companion.scatterFile
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityDatasource.Companion.separationFile
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Await
import java.io.File

class SeparabilityViewModel(
    repo: SeparabilityRepository = SeparabilityRepository()
): ApiViewModel(repo) {
    lateinit var craAwaiter: Await.CRAAwaiter

    lateinit var timePeriod: SeparabilityDatasource.TimePeriod
    lateinit var toAnalyze: RemoteCRAFileInfo
    lateinit var title: String
    lateinit var classes: List<String>
    lateinit var bandX: String
    lateinit var bandY: String

    var roiDir = File("")
    var roi: ROI = ROI()

    fun init(awaiter: Await.CRAAwaiter) {
        craAwaiter = awaiter
    }

    private var chartJob: Job? = null

    fun getSeparation(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        chartJob = getRemote(chartJob, makeCraROI(timePeriod.apiVal), callback) { api, roi ->
            timePeriod.separation(api, roi)
        }
    }
    fun saveSeparationFile(data: Map<String, Any>) = saveFile(separationFile(roiDir, timePeriod), data, JSONMap)
    fun loadSeparationFile(callback: (Result<Map<String, Any>>) -> Unit) = loadFile(separationFile(roiDir, timePeriod), JSONMap, callback)

    fun getScatter(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        chartJob = getRemote(chartJob, makeCraROI(timePeriod.apiVal), callback) { api, roi ->
            timePeriod.scatter(api, roi)
        }
    }
    fun saveScatterFile(data: Map<String, Any>, callback: (Result<Unit>) -> Unit) = saveFile(scatterFile(roiDir, timePeriod), data, JSONMap, callback)
    fun loadScatterFile(callback: (Result<Map<String, Any>>) -> Unit) = loadFile(scatterFile(roiDir, timePeriod), JSONMap, callback)

    fun getCorrelation(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        chartJob = getRemote(chartJob, makeCraROI(timePeriod.apiVal), callback) { api, roi ->
            timePeriod.correlation(api, roi)
        }
    }
    fun saveCorrelationFile(data: Map<String, Any>) = saveFile(correlationFile(roiDir, timePeriod), data, JSONMap)
    fun loadCorrelationFile(callback: (Result<Map<String, Any>>) -> Unit) = loadFile(correlationFile(roiDir, timePeriod), JSONMap, callback)

    private fun makeCraROI(timePeriod: Int): CraROI {
        return CraROI(timePeriod, toAnalyze.storageKey(), toAnalyze.numericClassField, toAnalyze.stringClassField, roi)
    }
}