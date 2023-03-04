package org.blueventures.gemdroid.model.cra

import com.github.zibnix.droidbones.mvvm.BaseViewModel
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.Shapefile
import java.io.File
import java.io.InputStream

class CraViewModel(private val repo: CraRepository = CraRepository()): BaseViewModel() {
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
        val contShp = Shapefile(cont.key(), cont.fields.chosenNumeric!!, cont.fields.chosenString!!)

        val hist = historicalCRA
        if (hist != null && hist.badFinalState()) {
            callback(Result.failure(badState))
            return
        }

        val histShp = if (hist == null) null else Shapefile(hist.key(), hist.fields.chosenNumeric!!, hist.fields.chosenString!!)
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