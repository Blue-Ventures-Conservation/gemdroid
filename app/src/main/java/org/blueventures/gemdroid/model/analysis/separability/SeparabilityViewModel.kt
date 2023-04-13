package org.blueventures.gemdroid.model.analysis.separability

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.CraROI
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.Shapefile
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File

class SeparabilityViewModel(private val repo: SeparabilityRepository = SeparabilityRepository()): ApiViewModel(repo) {
    var roiDir = File("")
    var roi: ROI = ROI()

    lateinit var timePeriod: TimePeriod
    lateinit var toAnalyze: Shapefile
    lateinit var title: String

    private var loadCRAsJob: Job? = null
    private var chartJob: Job? = null

    fun loadCRAs(callback: (Result<CRA>) -> Unit) {
        if (loadCRAsJob != null) return
        resultWithToken({ loadCRAsJob = it }, repo.loadCRAs(roiDir)) { result ->
            loadCRAsJob = null
            callback(result)
        }
    }

    fun getSeparation(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        if (chartJob != null) return
        apiWithToken({ chartJob = it }, repo.getSeparation(timePeriod, getCraROI())) { result ->
            chartJob = null
            callback(result)
        }
    }
    fun saveSeparationFile(data: Map<String, Any>) = scoped { repo.saveSeparationFile(roiDir, timePeriod, data).collect() }
    fun loadSeparationFile(callback: (Result<Map<String, Any>>) -> Unit) = scoped { repo.loadSeparationFile(roiDir, timePeriod).collect(callback) }

    fun getScatter(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        if (chartJob != null) return
        apiWithToken({ chartJob = it }, repo.getScatter(timePeriod, getCraROI())) { result ->
            chartJob = null
            callback(result)
        }
    }
    fun saveScatterFile(data: Map<String, Any>) = scoped { repo.saveScatterFile(roiDir, timePeriod, data).collect() }
    fun loadScatterFile(callback: (Result<Map<String, Any>>) -> Unit) = scoped { repo.loadScatterFile(roiDir, timePeriod).collect(callback) }

    fun getCorrelation(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        if (chartJob != null) return
        apiWithToken({ chartJob = it }, repo.getCorrelation(timePeriod, getCraROI())) { result ->
            chartJob = null
            callback(result)
        }
    }
    fun saveCorrelationFile(data: Map<String, Any>) = scoped { repo.saveCorrelationFile(roiDir, timePeriod, data).collect() }
    fun loadCorrelationFile(callback: (Result<Map<String, Any>>) -> Unit) = scoped { repo.loadCorrelationFile(roiDir, timePeriod).collect(callback) }

    private fun getCraROI(): CraROI {
        return CraROI(key = toAnalyze.shapefileStorageKey, numLabel = toAnalyze.numericClassField, charLabel = toAnalyze.stringClassField, roi = roi)
    }
}