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

    lateinit var cra: CRA
    lateinit var timePeriod: TimePeriod
    lateinit var toAnalyze: Shapefile
    lateinit var title: String
    lateinit var bandX: String
    lateinit var bandY: String

    private var awaitCRAsJob: Job? = null
    private var chartJob: Job? = null

    fun loadCRAs(callback: (Result<CRA>) -> Unit) = scoped { repo.loadCRAs(roiDir).collect(callback) }

    fun shouldAwaitCRAs(callback: (Result<Boolean>) -> Unit) = scoped { repo.shouldAwaitCRAs(roiDir).collect(callback) }

    fun awaitCRAs(callback: (Result<Throwable?>) -> Unit) {
        if (awaitCRAsJob != null) return
        resultWithToken({ awaitCRAsJob = it }, repo.awaitCRAs(roiDir, cra)) { result ->
            awaitCRAsJob = null
            callback(result)
        }
    }

    fun getSeparation(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        if (chartJob != null) return
        apiWithToken({ chartJob = it }, repo.getSeparation(timePeriod, getCraROI(timePeriod.apiVal))) { result ->
            chartJob = null
            callback(result)
        }
    }
    fun saveSeparationFile(data: Map<String, Any>) = scoped { repo.saveSeparationFile(roiDir, timePeriod, data).collect() }
    fun loadSeparationFile(callback: (Result<Map<String, Any>>) -> Unit) = scoped { repo.loadSeparationFile(roiDir, timePeriod).collect(callback) }

    fun getScatter(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        if (chartJob != null) return
        apiWithToken({ chartJob = it }, repo.getScatter(timePeriod, getCraROI(timePeriod.apiVal))) { result ->
            chartJob = null
            callback(result)
        }
    }
    fun saveScatterFile(data: Map<String, Any>, callback: (Result<Unit>) -> Unit) = scoped { repo.saveScatterFile(roiDir, timePeriod, data).collect(callback) }
    fun loadScatterFile(callback: (Result<Map<String, Any>>) -> Unit) = scoped { repo.loadScatterFile(roiDir, timePeriod).collect(callback) }

    fun getCorrelation(callback: (ApiResult<Map<String, Any>>) -> Unit) {
        if (chartJob != null) return
        apiWithToken({ chartJob = it }, repo.getCorrelation(timePeriod, getCraROI(timePeriod.apiVal))) { result ->
            chartJob = null
            callback(result)
        }
    }
    fun saveCorrelationFile(data: Map<String, Any>) = scoped { repo.saveCorrelationFile(roiDir, timePeriod, data).collect() }
    fun loadCorrelationFile(callback: (Result<Map<String, Any>>) -> Unit) = scoped { repo.loadCorrelationFile(roiDir, timePeriod).collect(callback) }

    private fun getCraROI(timePeriod: Int): CraROI {
        return CraROI(timePeriod, toAnalyze.shapefileStorageKey, toAnalyze.numericClassField, toAnalyze.stringClassField, roi)
    }
}