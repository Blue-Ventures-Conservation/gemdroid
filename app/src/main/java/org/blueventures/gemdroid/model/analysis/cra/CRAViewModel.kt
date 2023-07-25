package org.blueventures.gemdroid.model.analysis.cra

import com.github.zibnix.droidbones.NoStack
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File
import java.io.InputStream

class CRAViewModel(
    private val repo: CRARepository = CRARepository()
): CRAAwaiter, ApiViewModel(repo) {
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

    private var uploadJob: Job? = null

    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = scoped { repo.getRemoteCRAs().collect(callback) }
    fun validateLocalCRA(files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, callback: (Result<CRAFile>) -> Unit) = scoped {
        repo.validateLocalCRA(roiDir, files, names, remoteCRAs, previous).collect(callback)
    }

    fun getHistoricalChoices() = listOf(HistoricalChoice.SEPARATE, HistoricalChoice.NONE, HistoricalChoice.CONTEMPORARY)
    fun clearHistoricalChoice() { historicalChoice = HistoricalChoice.SEPARATE }

    fun getCRAFields(callback: (Result<Fields>) -> Unit) = scoped{ repo.getCRAFields(contemporaryCRA, historicalCRA).collect(callback) }
    fun setFields(fields: Fields) {
        contemporaryCRA.fields = fields
        historicalCRA?.fields = fields
    }

    fun saveCRAs(callback: (Result<Unit>) -> Unit) {
        val badState = NoStack(R.string.internal_cra_err)
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

        if (hist == null || cont.equivalent(hist)) {
            when {
                cont.isRemote() -> ingestContemporary(cont, hist, callback)
                cont.readyToUpload() -> uploadContemporary(cont, hist, callback)
            }
        } else {
            when {
                cont.isRemote() && hist.isRemote() -> ingestBoth(cont, hist, callback)
                cont.isRemote() && hist.readyToUpload() -> uploadEither(hist, cont, hist, callback)
                cont.readyToUpload() && hist.isRemote() -> uploadEither(cont, cont, hist, callback)
                cont.readyToUpload() && hist.readyToUpload() -> uploadBoth(cont, hist, callback)
            }
        }
    }

    private fun ingestContemporary(cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit) {
        skipUpload(cont, hist, callback, repo.ingestCRA(cont), repo.uploadFields(cont))
    }

    private fun uploadContemporary(cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit) {
        uploadIngest(cont, cont, hist, callback, repo.ingestCRA(cont))
    }

    // we ingest both here, because ingestion can fail somewhat silently
    private fun uploadEither(upload: CRAFile, cont: CRAFile, hist: CRAFile, callback: (Result<Unit>) -> Unit) {
        uploadIngest(upload, cont, hist, callback, repo.ingestCRAs(cont, hist))
    }

    private fun uploadIngest(upload: CRAFile, cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit, ingest: Flow<Result<Unit>>) {
        uploadIngestFields(cont, hist, callback, repo.uploadCRA(upload), ingest, repo.uploadFields(upload))
    }

    private fun uploadBoth(cont: CRAFile, hist: CRAFile, callback: (Result<Unit>) -> Unit) {
        uploadIngestFields(cont, hist, callback, repo.uploadCRAs(cont, hist), repo.ingestCRAs(cont, hist), repo.uploadFields(cont, hist))
    }

    private fun ingestBoth(cont: CRAFile, hist: CRAFile, callback: (Result<Unit>) -> Unit) {
        skipUpload(cont, hist, callback, repo.ingestCRAs(cont, hist), repo.uploadFields(cont, hist))
    }

    private fun skipUpload(cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit, ingest: Flow<Result<Unit>>, fields: Flow<Result<Unit>>) {
        uploadIngestFields(cont, hist, callback, flow { emit(Result.success(Unit)) }, ingest, fields)
    }

    private fun uploadIngestFields(cont: CRAFile, hist: CRAFile?, callback: (Result<Unit>) -> Unit, upload: Flow<Result<Unit>>, ingest: Flow<Result<Unit>>, fields: Flow<Result<Unit>>) {
        if (uploadJob != null) return

        resultWithToken<Unit>({ uploadJob = it }, flow {
            upload.collect { result ->
                when {
                    result.isSuccess -> ingest.collect { res ->
                        when {
                            res.isSuccess -> fields.collect { r ->
                                when {
                                    r.isSuccess -> repo.saveCRAs(roiDir, CRAFile.toCRA(cont, hist)).collect { emit(it) }
                                    else -> emit(r)
                                }
                            } else -> emit(res)
                        }
                    } else -> emit(result)
                }
            }
        }) { result ->
            uploadJob = null
            callback(result)
        }
    }

    fun clear() { clearHistoricalChoice() }

    /**
     * Following functions below are intended to be used by other packages for getting CRAs.
     */

    private var awaitCRAsJob: Job? = null

    override fun loadCRAs(callback: (Result<CRA>) -> Unit) = scoped { repo.loadCRAs(roiDir).collect(callback) }

    override fun shouldAwaitCRAs(callback: (Result<Boolean>) -> Unit) = scoped { repo.shouldAwaitCRAs(roiDir).collect(callback) }

    override fun awaitCRAs(cra: CRA, callback: (Result<Throwable?>) -> Unit) {
        if (awaitCRAsJob != null) return
        resultWithToken({ awaitCRAsJob = it }, repo.awaitCRAs(roiDir, cra)) { result ->
            awaitCRAsJob = null
            callback(result)
        }
    }
}

interface CRAAwaiter {
    fun loadCRAs(callback: (Result<CRA>) -> Unit): Job
    fun shouldAwaitCRAs(callback: (Result<Boolean>) -> Unit): Job
    fun awaitCRAs(cra: CRA, callback: (Result<Throwable?>) -> Unit)
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