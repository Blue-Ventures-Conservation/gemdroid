package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.Shapefile
import org.blueventures.gemdroid.data.VisualizeURLs
import java.io.File
import java.io.InputStream

class AnalysisViewModel(private val repo: AnalysisRepository = AnalysisRepository()): BaseViewModel() {
    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state

    var buffersJob: Job? = null
    var visualizeURLsJob: Job? = null

    fun refreshStage() = scoped {
        repo.getStage(_state.value.roiDir).collect { stage ->
            newState(_state.value.copy(stage = stage))
        }
    }

    fun getROI() = scoped {
        repo.getROI(_state.value.roiDir).collect { roi ->
            roi?.let {
                newState(_state.value.copy(roi = Result.success(it)))
            } ?: run {
                newState(_state.value.copy(roi = Result.failure(Throwable("Could not read filesystem state!"))))
            }
        }
    }

    fun saveBuffersFile(buffers: Buffers) = scoped {
        repo.saveBuffersFile(_state.value.roiDir, buffers).collect()
    }

    fun loadBuffersFile() = scoped {
        repo.loadBuffersFile(_state.value.roiDir).collect { buffers ->
            buffers?.let {
                newState(_state.value.copy(buffers = it))
            } ?: run {
                newState(_state.value.copy(buffers = Buffers.empty()))
            }
        }
    }

    fun getBuffers(roi: ROI, callback: () -> Unit = {}) {
        if (buffersJob != null) {
            return
        }

        buffersJob = scoped {
            repo.getBuffers(roi).collect { result ->
                newState(_state.value.copy(buffersResult = result))
                buffersJob = null
                callback()
            }
        }
    }

    fun saveBuffer(buffer: Int, callback: (Boolean) -> Unit) = scoped {
        val roiCpy = _state.value.roi!!.getOrNull()!!.copy(buffDist = buffer)
        repo.saveBuffer(_state.value.roiDir, roiCpy, buffer).collect {
            newState(_state.value.copy(roi = Result.success(roiCpy)))
            callback(it)
        }
    }

    fun saveVisualizeURLs(urls: VisualizeURLs) = scoped {
        repo.saveVisualizeURLs(_state.value.roiDir, urls).collect()
    }

    fun loadVisualizeURLs() = scoped {
        repo.loadVisualizeURLs(_state.value.roiDir).collect { buffers ->
            buffers?.let {
                newState(_state.value.copy(visualizeURLs = it))
            } ?: run {
                newState(_state.value.copy(visualizeURLs = VisualizeURLs.empty()))
            }
        }
    }

    fun getVisualizeURLs(roi: ROI, callback: () -> Unit = {}) {
        if (visualizeURLsJob != null) {
            return
        }

        visualizeURLsJob = scoped {
            repo.getVisualizeURLs(roi).collect { result ->
                newState(_state.value.copy(visualizeURLsResult = result))
                visualizeURLsJob = null
                callback()
            }
        }
    }

    fun getRemoteCRAs() = scoped {
        repo.getRemoteCRAs { result ->
            newState(_state.value.copy(remoteCRAs = result))
        }.collect()
    }

    fun validateLocalContemporaryCRA(roiDir: File, files: List<InputStream?>, names: List<String?>) = scoped {
        repo.validateLocalCRA(roiDir, files, names).collect {
            newState(_state.value.copy(localContemporaryCRA = it))
        }
    }

    fun validateLocalHistoricalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>) = scoped {
        repo.validateLocalCRA(roiDir, files, names).collect {
            newState(_state.value.copy(localHistoricalCRA = it))
        }
    }

    fun clearLocalContemporaryCRA() = newState(state.value.copy(localContemporaryCRA = null))
    fun clearLocalHistoricalCRA() = newState(state.value.copy(localHistoricalCRA = null))
    fun setContemporaryCRA(f: CRAFile) = newState(state.value.copy(contemporaryCRA = f))
    fun setHistoricalCRA(f: CRAFile?) = newState(state.value.copy(historicalCRA = f))
    fun getHistoricalChoices() = listOf(HistoricalChoice.SEPARATE, HistoricalChoice.NONE, HistoricalChoice.CONTEMPORARY)
    fun setHistoricalChoice(choice: HistoricalChoice) {
        var hist: CRAFile? = null
        if (choice == HistoricalChoice.CONTEMPORARY) {
            hist = _state.value.contemporaryCRA
        }
        newState(_state.value.copy(historicalChoice = choice, historicalCRA = hist))
    }
    fun clearHistoricalChoice() = newState(_state.value.copy(historicalChoice = HistoricalChoice.SEPARATE))

    fun getCRAFields() = scoped {
        _state.value.contemporaryCRA?.let { cont ->
            repo.getCRAFields(cont, _state.value.historicalCRA) { result ->
                newState(_state.value.copy(fields = result))
            }.collect()
        }
    }

    fun setFields(fields: Fields) {
        val cont = _state.value.contemporaryCRA?.copy(fields = fields) ?: CRAFile(fields = fields)
        val hist = _state.value.historicalCRA?.copy(fields = fields)
        newState(_state.value.copy(contemporaryCRA = cont, historicalCRA = hist))
    }

    fun saveCRAs(callback: (Result<Unit>) -> Unit) {
        val badState = Throwable("Internal CRA data error, sorry!")
        val cont = _state.value.contemporaryCRA

        if (cont == null || cont.badFinalState()) {
            callback(Result.failure(badState))
            return
        }
        val contShp = Shapefile(cont.key(), cont.fields.numeric!!, cont.fields.string!!)

        val hist = _state.value.historicalCRA
        if (hist != null && hist.badFinalState()) {
            callback(Result.failure(badState))
            return
        }
        val histShp = if (hist == null) {
            null
        } else {
            Shapefile(hist.key(), hist.fields.numeric!!, hist.fields.string!!)
        }

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
                repo.uploadCRAs(cont, hist) {
                    when {
                        it.isSuccess -> {
                            saveCRAs(cra, callback)
                        }
                        it.isFailure -> callback(it)
                    }
                }
            }
        }
    }

    private fun uploadThenSaveCRA(upload: CRAFile, cra: CRA, callback: (Result<Unit>) -> Unit) {
        uploadCRA(upload) {
            when {
                it.isSuccess -> {
                    saveCRAs(cra, callback)
                }
                it.isFailure -> callback(it)
            }
        }
    }

    private fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) = scoped { repo.uploadCRA(cra, callback).collect() }

    private fun saveCRAs(cra: CRA, callback: (Result<Unit>) -> Unit) = scoped {
        repo.saveCRAs(_state.value.roiDir, cra).collect { success ->
            if (success) {
                callback(Result.success(Unit))
            } else {
                callback(Result.failure(Throwable("Could not save CRA metadata locally")))
            }
        }
    }

    fun setRoiDir(dir: File) = newState(AnalysisState(roiDir = dir))
    fun clearVisualizeURLs() = newState(_state.value.copy(visualizeURLs = VisualizeURLs.empty(), visualizeURLsResult = null))
    fun clearBuffers() = newState(_state.value.copy(buffers = null, buffersResult = null))
    fun clearStage() = newState(_state.value.copy(stage = null))
    fun clear() = newState(AnalysisState(_state.value.roiDir))
    fun chotTileDir(): File = repo.chotTileDir(_state.value.roiDir)
    fun clotTileDir(): File = repo.clotTileDir(_state.value.roiDir)
    fun hhotTileDir(): File = repo.hhotTileDir(_state.value.roiDir)
    fun hlotTileDir(): File = repo.hlotTileDir(_state.value.roiDir)
    private fun newState(state: AnalysisState) { _state.value = state }
}

data class AnalysisState(
    val roiDir: File = File(""),
    val stage: Stage? = null,
    val roi: Result<ROI>? = null,
    val buffers: Buffers? = null,
    val buffersResult: ApiResult<Buffers>? = null,
    val visualizeURLs: VisualizeURLs? = null,
    val visualizeURLsResult: ApiResult<VisualizeURLs>? = null,
    val remoteCRAs: Result<List<String>>? = null,
    val localContemporaryCRA: Result<CRAFile>? = null,
    val contemporaryCRA: CRAFile? = null,
    val historicalChoice: HistoricalChoice = HistoricalChoice.SEPARATE,
    val localHistoricalCRA: Result<CRAFile>? = null,
    val historicalCRA: CRAFile? = null,
    val fields: Result<Fields>? = null
)

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