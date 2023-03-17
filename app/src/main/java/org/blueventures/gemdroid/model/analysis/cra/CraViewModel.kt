package org.blueventures.gemdroid.model.analysis.cra

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

class CraViewModel(private val repo: CraRepository = CraRepository()): ApiViewModel(repo) {
    var roiDir = File("")
    var contemporaryCRA: CRAFile = CRAFile()
    var historicalCRA: CRAFile? = null
    var historicalChoice: HistoricalChoice = HistoricalChoice.SEPARATE
        set(choice) {
            field = choice
            var hist: CRAFile? = null
            if (choice == HistoricalChoice.CONTEMPORARY) hist = contemporaryCRA
            historicalCRA = hist
        }

    val craIngestJobs = mutableMapOf<String, Job>()
    val craAwaitIngestJobs = mutableMapOf<String, Job>()

    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = scoped { repo.getRemoteCRAs(callback).collect() }
    fun validateLocalCRA(files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, callback: (Result<CRAFile>) -> Unit) = scoped {
        repo.validateLocalCRA(roiDir, files, names, remoteCRAs, previous).collect(callback)
    }

    fun getHistoricalChoices() = listOf(HistoricalChoice.SEPARATE, HistoricalChoice.NONE, HistoricalChoice.CONTEMPORARY)
    fun clearHistoricalChoice() { historicalChoice = HistoricalChoice.SEPARATE }

    fun getCRAFields(callback: (Result<Fields>) -> Unit) = scoped { repo.getCRAFields(contemporaryCRA, historicalCRA, callback).collect() }
    fun setFields(fields: Fields) {
        contemporaryCRA.fields = fields
        historicalCRA?.fields = fields
    }

    fun saveCRAs(callback: (Result<Unit>) -> Unit) {
        val badState = Throwable("Internal CRA data error, sorry!")
        val cont = contemporaryCRA

        if (cont.badFinalState()) {
            callback(Result.failure(badState))
            return
        }

        val hist = historicalCRA
        if (hist != null && hist.badFinalState()) {
            callback(Result.failure(badState))
            return
        }

        when {
            cont.isRemote() && (hist == null || cont.equivalent(hist) || hist.isRemote()) -> {
                // both remote, fields should already be saved
                ingestCRAs(cont, hist) { result ->
                    when {
                        result.isSuccess -> saveCRAs(CRAFile.toCRA(cont, hist), callback)
                        else -> callback(result)
                    }
                }
            }
            cont.isRemote() && (hist?.readyToUpload() == true) -> {
                // upload hist
                uploadIngestSave(hist, cont, hist, callback)
            }
            cont.readyToUpload() && (hist == null || cont.equivalent(hist) || hist.isRemote()) -> {
                // upload cont
                uploadIngestSave(cont, cont, hist, callback)
            }
            cont.readyToUpload() && (hist?.readyToUpload() == true) -> {
                // upload both
                scoped {
                    repo.uploadCRAs(cont, hist) { result ->
                        when {
                            result.isSuccess -> ingestCRAs(cont, hist) { res ->
                                when {
                                    res.isSuccess -> uploadFields(cont, hist) { r ->
                                        when {
                                            r.isSuccess -> saveCRAs(CRAFile.toCRA(cont, hist), callback)
                                            else -> callback(r)
                                        }
                                    }
                                    else -> callback(res)
                                }
                            }
                            else -> callback(result)
                        }
                    }.collect()
                }
            }
        }
    }

    // we always try to ingest, even if the CRA is already remote, as ingestion may fail somewhat silently
    private fun ingestCRAs(cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit) {
        if (hist == null || cont.equivalent(hist)) {
            ingestCRA(cont) { result ->
                when {
                    result.isSuccess -> {
                        val name = result.getOrNull()!!
                        cont.eeUploadName = name
                        hist?.eeUploadName = name
                        callback(Result.success(Unit))
                    }
                    else -> callback(Result.failure(result.exceptionOrNull()!!))
                }
            }
            return
        }

        val successes = AtomicInteger()
        val failures = AtomicInteger()

        val handleErr: (Result<String>) -> Boolean = { result ->
            if (result.isFailure && failures.addAndGet(1) == 1) {
                callback(Result.failure(result.exceptionOrNull()!!))
                true
            } else {
                false
            }
        }

        val handleResult: ((String?) -> Unit) -> (Result<String>) -> Unit = { setter -> { result ->
            if (!handleErr(result)) {
                setter(result.getOrNull())
                if (successes.addAndGet(1) == 2) {
                    callback(Result.success(Unit))
                }
            }
        }}

        ingestCRA(cont, handleResult { cont.eeUploadName = it })
        ingestCRA(hist, handleResult { hist.eeUploadName = it })
    }

    private fun ingestCRA(cra: CRAFile, callback: (Result<String>) -> Unit) {
        val key = cra.key()
        ingestNeeded(cra.eeUploadName, key) { result ->
            when {
                result.isSuccess -> {
                    if (result.getOrNull()!!) {
                        if (!craIngestJobs.contains(key)) {
                            withToken({ craIngestJobs[key] = it }, { repo.ingestCRATable(key) }) { res ->
                                craIngestJobs.remove(key)
                                when (res) {
                                    is ApiResult.Success -> {
                                        val data = res.data!!
                                        when (data.success) {
                                            true -> callback(Result.success(data.name))
                                            else -> callback(Result.failure(Throwable("Ingestion of CRA into Earth Engine failed.")))
                                        }
                                    }
                                    else -> callback(Result.failure(Throwable(res.message!!)))
                                }
                            }
                        }
                    } else {
                        callback(Result.success(cra.eeUploadName!!))
                    }
                }
                else -> callback(Result.failure(result.exceptionOrNull()!!))
            }
        }
    }

    private fun ingestNeeded(name: String?, key: String, callback: (Result<Boolean>) -> Unit) {
        if (name == "") {
            callback(Result.success(false))
            return
        }

        when (name) {
            null -> callback(Result.success(true))
            else -> {
                if (craAwaitIngestJobs.contains(name)) return
                withToken({ craAwaitIngestJobs[name] = it }, { repo.awaitCRAIngestion(name, key) }) { result ->
                    craAwaitIngestJobs.remove(name)
                    when (result) {
                        is ApiResult.Success -> callback(Result.success(!result.data!!.success))
                        else -> callback(Result.failure(Throwable(result.message!!)))
                    }
                }
            }
        }
    }

    private fun uploadIngestSave(upload: CRAFile, cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit) {
        uploadCRA(upload) { result ->
            when {
                result.isSuccess -> ingestCRAs(cont, hist) { res ->
                    when {
                        res.isSuccess -> uploadFields(upload) { r ->
                            when {
                                r.isSuccess -> saveCRAs(CRAFile.toCRA(cont, hist), callback)
                                else -> callback(r)
                            }
                        }
                        else -> callback(res)
                    }
                }
                else -> callback(result)
            }
        }
    }

    private fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) = scoped { repo.uploadCRA(cra, callback).collect() }
    private fun uploadFields(cra: CRAFile, callback: (Result<Unit>) -> Unit) = scoped { repo.uploadFields(cra, callback) }
    private fun uploadFields(cont: CRAFile, hist: CRAFile, callback: (Result<Unit>) -> Unit) = scoped { repo.uploadFields(cont, hist, callback) }
    private fun saveCRAs(cra: CRA, callback: (Result<Unit>) -> Unit) = scoped { repo.saveCRAs(roiDir, cra).collect(callback) }

    fun clear() { clearHistoricalChoice() }
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