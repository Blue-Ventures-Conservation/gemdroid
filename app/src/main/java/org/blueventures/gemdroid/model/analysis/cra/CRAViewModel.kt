package org.blueventures.gemdroid.model.analysis.cra

import android.icu.text.SimpleDateFormat
import com.github.zibnix.droidbones.NoStack
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.GeojsonPolygonFeature
import org.blueventures.gemdroid.data.GeojsonPolygonFeatureCollection
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.data.analysis.cra.BothFieldsCounted
import org.blueventures.gemdroid.data.analysis.cra.ClassCount
import org.blueventures.gemdroid.data.analysis.cra.ClassCounts
import org.blueventures.gemdroid.data.analysis.cra.ContemporaryAndHistoricalCRAs
import org.blueventures.gemdroid.data.analysis.cra.Fields
import org.blueventures.gemdroid.data.analysis.cra.FieldsCounts
import org.blueventures.gemdroid.data.analysis.cra.LocalOrRemoteCRAFile
import org.blueventures.gemdroid.data.analysis.cra.StringsNumerics
import org.blueventures.gemdroid.data.classNamePropertyKey
import org.blueventures.gemdroid.data.classNumberPropertyKey
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.PolygonGrouper
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.createdCRAFile
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.renamedCreatedCRAFile
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.maps.Capture
import org.blueventures.gemdroid.ui.common.maps.Visualize
import java.io.File
import java.io.InputStream
import java.util.Date
import java.util.Locale

class CRAViewModel(
    private val repo: CRARepository = CRARepository()
): Capture.Data, Await.CRAAwaiter, ApiViewModel(repo) {
    lateinit var grouper: PolygonGrouper

    var roiDir = File("")
    var roi: ROI = ROI()
    var visualizer: Visualize.Visualizer? = null
    var historicalCRA: LocalOrRemoteCRAFile? = null
    var contemporaryCRA = LocalOrRemoteCRAFile()
    var historicalChoice = HistoricalChoice.SEPARATE
        set(choice) {
            field = choice
            var hist: LocalOrRemoteCRAFile? = null
            if (choice == HistoricalChoice.CONTEMPORARY) hist = contemporaryCRA
            historicalCRA = hist
        }

    fun init(visualizer: Visualize.Visualizer, grouper: PolygonGrouper) {
        this.visualizer = visualizer
        this.grouper = grouper
    }

    fun loadLocallyCreatedCRAFile(callback: (Result<GeojsonPolygonFeatureCollection>) -> Unit) = loadFile(createdCRAFile(roiDir), GeojsonPolygonFeatureCollection.Companion, callback)
    fun saveLocallyCreatedCRAFile(callback: (Result<Unit>) -> Unit) = saveFile(createdCRAFile(roiDir), capturedCollection, GeojsonPolygonFeatureCollection.Companion, callback)
    fun deleteLocallyCreatedCRAFile(callback: (Result<Unit>) -> Unit) = deleteFile(createdCRAFile(roiDir), callback)

    private var uploadJob: Job? = null

    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = scoped { repo.getRemoteCRAs().collect(callback) }
    fun validateLocalCRA(files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean, callback: (Result<LocalOrRemoteCRAFile>) -> Unit) = scoped {
        repo.validateLocalCRA(roiDir, files, names, remoteCRAs, previous, overwrite).collect(callback)
    }

    fun getHistoricalChoices() = listOf(HistoricalChoice.SEPARATE, HistoricalChoice.NONE, HistoricalChoice.CONTEMPORARY)
    fun clearHistoricalChoice() { historicalChoice = HistoricalChoice.SEPARATE }

    fun getCRAFields(callback: (Result<BothFieldsCounted>) -> Unit) = scoped{ repo.getCRAFields(historicalCRA, contemporaryCRA).collect(callback) }
    fun setFields(both: BothFieldsCounted) {
        historicalCRA?.counted?.fields = both.fields
        historicalCRA?.counted?.counts = both.histCounts
        contemporaryCRA.counted.fields = both.fields
        contemporaryCRA.counted.counts = both.contCounts
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

    private fun ingestContemporary(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit) {
        skipUpload(cont, hist, callback, repo.ingestCRA(cont), repo.uploadFields(cont))
    }

    private fun uploadContemporary(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit) {
        uploadIngest(cont, cont, hist, callback, repo.ingestCRA(cont))
    }

    // we ingest both here, because ingestion can fail somewhat silently
    private fun uploadEither(upload: LocalOrRemoteCRAFile, cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile, callback: (Result<Unit>) -> Unit) {
        uploadIngest(upload, cont, hist, callback, repo.ingestCRAs(roiDir, cont, hist))
    }

    private fun uploadIngest(upload: LocalOrRemoteCRAFile, cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit, ingest: Flow<Result<Unit>>) {
        uploadIngestFields(cont, hist, callback, repo.uploadCRA(upload), ingest, repo.uploadFields(upload))
    }

    private fun uploadBoth(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile, callback: (Result<Unit>) -> Unit) {
        uploadIngestFields(cont, hist, callback, repo.uploadCRAs(cont, hist), repo.ingestCRAs(roiDir, cont, hist), repo.uploadFields(cont, hist))
    }

    private fun ingestBoth(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile, callback: (Result<Unit>) -> Unit) {
        skipUpload(cont, hist, callback, repo.ingestCRAs(roiDir, cont, hist), repo.uploadFields(cont, hist))
    }

    private fun skipUpload(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit, ingest: Flow<Result<Unit>>, fields: Flow<Result<Unit>>) {
        uploadIngestFields(cont, hist, callback, flow { emit(Result.success(Unit)) }, ingest, fields)
    }

    private fun uploadIngestFields(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit, upload: Flow<Result<Unit>>, ingest: Flow<Result<Unit>>, fields: Flow<Result<Unit>>) {
        uploadJob = resultWithToken<Unit>(uploadJob, flow {
            upload.collect { result ->
                when {
                    result.isSuccess -> ingest.collect { res ->
                        when {
                            res.isSuccess -> fields.collect { r ->
                                when {
                                    r.isSuccess -> repo.saveCRAs(roiDir, hist, cont).collect { emit(it) }
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

    fun clearState() { clearHistoricalChoice() }

    fun handleLocalCounts(counts: StringsNumerics, chosenString: String, chosenStrings: List<String>, chosenNumeric: String, chosenNumerics: List<Int>): Pair<List<ClassCount>, Int?> {
        val zipped = chosenNumerics.zip(chosenStrings)
        val strAssociated = zipped.associateByTo(mutableMapOf(), {
            it.second
        }) {
            it.first
        }

        val strings = counts.stringCounts.getChosenCounts(chosenString, strAssociated)
        val numbers = counts.numericCounts.getChosenCounts(chosenNumeric, null)
        compareFields(strings, numbers)?.let { msg ->
            return Pair(emptyList(), msg)
        }

        return Pair(strings, null)
    }

    fun compareCRAs(histStrings: List<ClassCount>, contStrings: List<ClassCount>): Int? {
        val histPairs = histStrings.associateByTo(mutableMapOf(), {
            it.classNumber
        }) {
            it.className
        }
        val contPairs = contStrings.associateByTo(mutableMapOf(), {
            it.classNumber
        }) {
            it.className
        }

        var errMsg: Int? = null
        for (entry in histPairs.entries) {
            val c = contPairs[entry.key]
            if (c == null || c != entry.value) {
                errMsg = R.string.class_names_and_values_do_not_match
            }
        }

        return errMsg
    }

    private fun compareFields(strings: List<ClassCount>, numerics: List<ClassCount>): Int? {
        for (sc in strings) {
            for (nc in numerics) {
                if (sc.classNumber == nc.classNumber) {
                    if (sc.craCount != nc.craCount) {
                        return R.string.mismatched_class_name_and_class_value
                    }
                    break
                }
            }
        }

        return null
    }

    /**
     * Following functions below are intended to be used by other packages for getting CRAs.
     */

    private var awaitCRAsJob: Job? = null

    override fun loadCRAs(callback: (Result<ContemporaryAndHistoricalCRAs>) -> Unit) = scoped { repo.loadCRAs(roiDir).collect(callback) }

    override fun shouldAwaitCRAs(callback: (Result<Boolean>) -> Unit) = scoped { repo.shouldAwaitCRAs(roiDir).collect(callback) }

    override fun awaitCRAs(cras: ContemporaryAndHistoricalCRAs, callback: (Result<Unit>) -> Unit) {
        awaitCRAsJob = resultWithToken(awaitCRAsJob, repo.awaitCRAs(roiDir, cras), callback)
    }

    override val classes = BVClass.entries
    override val capturedCollection
        get() = GeojsonPolygonFeatureCollection(features = capturedFeatures)
    val capturedFeatures = mutableListOf<GeojsonPolygonFeature>()
    override fun capture(craClass: BVClass, polygon: List<LatLng>, callback: (Result<Unit>) -> Unit) {
        capturedFeatures.add(GeojsonPolygonFeature(
            geometry = GeojsonPolygon.fromState(listOf(polygon)),
            properties = mapOf(
                classNamePropertyKey to craClass.enNames.first(),
                classNumberPropertyKey to craClass.number,
            )
        ))
        saveLocallyCreatedCRAFile(callback)
    }

    fun continueExisting(fc: GeojsonPolygonFeatureCollection) {
        capturedFeatures.clear()
        capturedFeatures.addAll(fc.features)
    }

    fun processAndSaveCapturedCRAs(callback: (Result<Unit>) -> Unit) {
        historicalCRA = null
        val classCountList = capturedCollection.countClasses()
        val classCounts = ClassCounts(chosenCounts = classCountList)
        val timestamp = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.getDefault()).format(Date())
        val noExt = createdCRAFile.substringBeforeLast(".")
        val newName = "${noExt}_${timestamp}.geojson"
        renameFile(createdCRAFile(roiDir), newName) { renameResult ->
            when {
                renameResult.isFailure -> callback(renameResult)
                else -> {
                    contemporaryCRA = LocalOrRemoteCRAFile(
                        false,
                        null,
                        false,
                        null,
                        renamedCreatedCRAFile(roiDir, newName),
                        FieldsCounts(
                            Fields(
                                chosenNumeric = classNumberPropertyKey,
                                chosenString = classNamePropertyKey,
                                chosenStringValues = classCountList.map { it.className },
                            ),
                            StringsNumerics(classCounts, classCounts)
                        )
                    )
                    saveCRAs(callback)
                }
            }
        }
    }
}

/**
 * Answers to the question: Are historical CRAs available?
 */
enum class HistoricalChoice {
    SEPARATE {
        override fun label() = R.string.historical_choice_yes
    },

    NONE {
        override fun label() = R.string.historical_choice_no
    },

    CONTEMPORARY {
        override fun label() = R.string.historical_choice_not_necessary
    };

    abstract fun label(): Int
}