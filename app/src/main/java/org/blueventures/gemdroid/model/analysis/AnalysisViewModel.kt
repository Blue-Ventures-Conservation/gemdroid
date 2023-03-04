package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.Shapefile
import org.blueventures.gemdroid.data.VisualizeURLs
import java.io.File
import java.io.InputStream

class AnalysisViewModel(private val repo: AnalysisRepository = AnalysisRepository()): BaseViewModel() {
    var roiDir = File("")
    var roi = ROI()
    var contemporaryCRA: CRAFile = CRAFile()
    var historicalCRA: CRAFile? = null
    var historicalChoice: HistoricalChoice = HistoricalChoice.SEPARATE
        set(choice) {
            field = choice
            var hist: CRAFile? = null
            if (choice == HistoricalChoice.CONTEMPORARY) hist = contemporaryCRA
            historicalCRA = hist
        }

    var buffersJob: Job? = null
    var visualizeURLsJob: Job? = null

    fun refreshStage(callback: (Stage) -> Unit) = scoped { repo.getStage(roiDir).collect(callback) }
    fun getROI(callback: (Result<ROI>) -> Unit) = scoped { repo.getROI(roiDir).collect(callback) }

    fun saveBuffersFile(buffers: Buffers) = scoped { repo.saveBuffersFile(roiDir, buffers).collect() }
    fun loadBuffersFile(callback: (Result<Buffers>) -> Unit) = scoped { repo.loadBuffersFile(roiDir).collect(callback) }
    fun getBuffers(callback: (ApiResult<Buffers>) -> Unit = {}) {
        if (buffersJob != null) return
        buffersJob = scoped {
            repo.getBuffers(roi).collect { result ->
                buffersJob = null
                callback(result)
            }
        }
    }

    fun saveBuffer(buffer: Int, callback: (Result<Unit>) -> Unit) = scoped {
        val roiCpy = roi.copy(buffDist = buffer)
        repo.saveBuffer(roiDir, roiCpy, buffer).collect(callback)
    }

    fun saveVisualizeURLsFile(urls: VisualizeURLs) = scoped { repo.saveVisualizeURLs(roiDir, urls).collect() }
    fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit) = scoped { repo.loadVisualizeURLs(roiDir).collect(callback) }
    fun getVisualizeURLs(callback: (ApiResult<VisualizeURLs>) -> Unit = {}) {
        if (visualizeURLsJob != null) return
        visualizeURLsJob = scoped {
            repo.getVisualizeURLs(roi).collect { result ->
                visualizeURLsJob = null
                callback(result)
            }
        }
    }

    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = scoped { repo.getRemoteCRAs(callback).collect() }
    fun validateLocalCRA(files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, callback: (Result<CRAFile>) -> Unit) = scoped {
        repo.validateLocalCRA(roiDir, files, names, remoteCRAs, previous).collect(callback)
    }

    fun getHistoricalChoices() = listOf(HistoricalChoice.SEPARATE, HistoricalChoice.NONE, HistoricalChoice.CONTEMPORARY)
    fun clearHistoricalChoice() { historicalChoice = HistoricalChoice.SEPARATE }

    fun getCRAFields(callback: (Result<Fields>) -> Unit) = scoped { repo.getCRAFields(contemporaryCRA, historicalCRA, callback).collect() }
    fun setFields(fields: Fields) {
        contemporaryCRA = contemporaryCRA.copy(fields = fields)
        historicalCRA = historicalCRA?.copy(fields = fields)
    }

    fun saveCRAs(callback: (Result<Unit>) -> Unit) {
        val badState = Throwable("Internal CRA data error, sorry!")
        val cont = contemporaryCRA

        if (cont.badFinalState()) {
            callback(Result.failure(badState))
            return
        }
        val contShp = Shapefile(cont.key(), cont.fields.numeric!!, cont.fields.string!!)

        val hist = historicalCRA
        if (hist != null && hist.badFinalState()) {
            callback(Result.failure(badState))
            return
        }

        val histShp = if (hist == null) null else Shapefile(hist.key(), hist.fields.numeric!!, hist.fields.string!!)
        val cra = CRA(contShp, histShp)

        when {
            cont.isRemote() && (hist == null || cont.equivalent(hist) || hist.isRemote()) -> {
                // both remote, save
                saveCRAs(cra, callback)
            }
            cont.isRemote() && (hist?.readyToUpload() == true) -> {
                // upload hist
                uploadThenSaveCRA(hist, cra, callback)
            }
            cont.readyToUpload() && (hist == null || cont.equivalent(hist) || hist.isRemote()) -> {
                // upload cont
                uploadThenSaveCRA(cont, cra, callback)
            }
            cont.readyToUpload() && (hist?.readyToUpload() == true) -> {
                // upload both
                scoped {
                    repo.uploadCRAs(cont, hist) { result ->
                        when {
                            result.isSuccess -> saveCRAs(cra, callback)
                            result.isFailure -> callback(result)
                        }
                    }.collect()
                }
            }
        }
    }

    private fun uploadThenSaveCRA(upload: CRAFile, cra: CRA, callback: (Result<Unit>) -> Unit) {
        uploadCRA(upload) { result ->
            when {
                result.isSuccess -> saveCRAs(cra, callback)
                result.isFailure -> callback(result)
            }
        }
    }

    private fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) = scoped { repo.uploadCRA(cra, callback).collect() }
    private fun saveCRAs(cra: CRA, callback: (Result<Unit>) -> Unit) = scoped { repo.saveCRAs(roiDir, cra).collect(callback) }

    fun clear() { clearHistoricalChoice() }
    fun chotTileDir(): File = repo.chotTileDir(roiDir)
    fun clotTileDir(): File = repo.clotTileDir(roiDir)
    fun hhotTileDir(): File = repo.hhotTileDir(roiDir)
    fun hlotTileDir(): File = repo.hlotTileDir(roiDir)
}

/**
 * Answers to the question: Are historical CRAs available?
 */
enum class HistoricalChoice {
    SEPARATE {
        override fun label() = "Yes, I have a separate shapefile for historical CRAs."
    },

    NONE {
        override fun label() = "No, train the classifier only on contemporary imagery."
    },

    CONTEMPORARY {
        override fun label() = "The CRAs haven't changed between historical and contemporary time periods. Reuse the contemporary CRAs for historical imagery."
    };

    abstract fun label(): String
}