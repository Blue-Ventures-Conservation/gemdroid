package org.blueventures.gemdroid.model.analysis.cra

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.github.zibnix.droidbones.NoStack
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonMultiPolygon
import org.blueventures.gemdroid.data.GeojsonPolygon
import org.blueventures.gemdroid.data.GeojsonPolygonFeature
import org.blueventures.gemdroid.data.GeojsonPolygonFeatureCollection
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.data.analysis.CRAClass
import org.blueventures.gemdroid.data.analysis.cra.BothFieldsCounted
import org.blueventures.gemdroid.data.analysis.cra.ClassCount
import org.blueventures.gemdroid.data.analysis.cra.ClassCounts
import org.blueventures.gemdroid.data.analysis.cra.ContemporaryAndHistoricalCRAs
import org.blueventures.gemdroid.data.analysis.cra.Fields
import org.blueventures.gemdroid.data.analysis.cra.FieldsCounts
import org.blueventures.gemdroid.data.analysis.cra.LocalOrRemoteCRAFile
import org.blueventures.gemdroid.data.analysis.cra.StringsNumerics
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.PolygonGrouper
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classIDPropertyKey
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classNamePropertyKey
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classNumberPropertyKey
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.creationGeojson
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.maps.Capture
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.maps.Visualize
import org.blueventures.gemdroid.ui.theme.Chartreuse
import java.io.File
import java.io.InputStream

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

    fun loadLocallyCreatedCRAFile(callback: (Result<GeojsonPolygonFeatureCollection>) -> Unit) = loadFile(creationGeojson(roiDir), GeojsonPolygonFeatureCollection.Companion, callback)
    fun saveLocallyCreatedCRAFile(callback: (Result<Unit>) -> Unit) = saveFile(creationGeojson(roiDir), capturedCollection, GeojsonPolygonFeatureCollection.Companion, callback)
    fun deleteLocallyCreatedCRAFile(callback: (Result<Unit>) -> Unit) {
        deleteFile(creationGeojson(roiDir)) {
            capturedFeatures.clear()
            callback(it)
        }
    }

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

    fun crasToCloudStorage(callback: (Result<Unit>) -> Unit) {
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
                // we ingest both here, because ingestion can fail somewhat silently
                cont.isRemote() && hist.isRemote() -> ingestBoth(cont, hist, callback)
                cont.isRemote() && hist.readyToUpload() -> uploadOneIngestOther(cont, hist, callback, hist, cont)
                cont.readyToUpload() && hist.isRemote() -> uploadOneIngestOther(cont, hist, callback, cont, hist)
                cont.readyToUpload() && hist.readyToUpload() -> uploadBoth(cont, hist, callback)
            }
        }
    }

    /**
     * Uploading includes ingesting, so if you upload a CRA, you don't need to also ingest it here.
     */

    private fun ingestContemporary(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit) {
        collectCRAFlow(cont, hist, callback, repo.ingestCRA(cont))
    }

    private fun uploadContemporary(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit) {
        collectCRAFlow(cont, hist, callback, repo.uploadCRA(cont))
    }

    private fun ingestBoth(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile, callback: (Result<Unit>) -> Unit) {
        collectCRAFlow(cont, hist, callback, repo.ingestCRAs(cont, hist))
    }

    private fun uploadOneIngestOther(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile, callback: (Result<Unit>) -> Unit, toUpload: LocalOrRemoteCRAFile, toIngest: LocalOrRemoteCRAFile) {
        collectCRAFlow(cont, hist, callback, repo.uploadOneIngestOther(toUpload, toIngest))
    }

    private fun uploadBoth(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile, callback: (Result<Unit>) -> Unit) {
        collectCRAFlow(cont, hist, callback, repo.uploadCRAs(cont, hist))
    }

    private fun collectCRAFlow(cont: LocalOrRemoteCRAFile, hist: LocalOrRemoteCRAFile?, callback: (Result<Unit>) -> Unit, theFlow: Flow<Result<Unit>>) {
        uploadJob = resultWithToken(uploadJob, flow {
            theFlow.collect { result ->
                when {
                    result.isSuccess -> repo.saveCRAs(roiDir, hist, cont).collect { emit(it) }
                    else -> emit(result)
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

    override val capturedCollection
        get() = GeojsonPolygonFeatureCollection(features = capturedFeatures)
    val capturedFeatures = mutableListOf<GeojsonPolygonFeature>()
    override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) {
        background({
            val polygons = mutableListOf<Polygons.Named>()
            capturedCollection.features.forEach { feature ->
                identity(feature)?.let { stringID ->
                    polygons.add(Polygons.Named(stringID, GeojsonMultiPolygon.toState(GeojsonMultiPolygon(listOf(feature.geometry.coordinates)))))
                }
            }

            if (polygons.isEmpty()) {
                emptyList()
            } else {
                val color = Chartreuse.toArgb()
                listOf(Polygons.Group("CRAs", polygons, color, color))
            }
        }, callback)
    }

    var currentID = 1
        get() = field++
    private var craClasses: List<CRAClass>? = null
    override fun classes(context: Context): List<CRAClass> {
        if (craClasses == null) {
            craClasses = BVClass.entries.map { it.toCRAClass(context) }
        }
        return craClasses!!
    }
    override fun capture(craClass: CRAClass, polygon: List<LatLng>, callback: (Result<Unit>) -> Unit) {
        capturedFeatures.add(GeojsonPolygonFeature(
            geometry = GeojsonPolygon.fromState(listOf(polygon)),
            properties = makeProperties(currentID, craClass)
        ))
        saveLocallyCreatedCRAFile(callback)
    }
    private fun makeProperties(id: Int, craClass: CRAClass) = mapOf(
        classIDPropertyKey to id,
        classNamePropertyKey to craClass.name,
        classNumberPropertyKey to craClass.number,
    )
    override fun identity(feature: GeojsonPolygonFeature) = feature.intProperty(classIDPropertyKey)?.toString()
    override fun currentClass(id: String): String? {
        for (feature in capturedFeatures) {
            if (feature.intProperty(classIDPropertyKey)?.toString() == id) {
                return (feature.properties[classNamePropertyKey] as? String)
            }
        }

        return null
    }
    override fun updateClass(id: String, newClass: CRAClass, callback: (Result<Unit>) -> Unit) {
        findFeature(id) { intID, feature ->
            feature.properties = makeProperties(intID, newClass)
            saveLocallyCreatedCRAFile(callback)
        }
    }
    override fun delete(id: String, callback: (Result<Unit>) -> Unit) {
        findFeature(id) { _, feature ->
            capturedFeatures.remove(feature)
            saveLocallyCreatedCRAFile(callback)
        }
    }
    private fun findFeature(id: String, found: (Int, GeojsonPolygonFeature) -> Unit) {
        for (feature in capturedFeatures) {
            feature.intProperty(classIDPropertyKey)?.let { intID ->
                if (intID.toString() == id) {
                    found(intID, feature)
                    return
                }
            }
        }
    }

    fun continueExisting(fc: GeojsonPolygonFeatureCollection) {
        capturedFeatures.clear()
        capturedFeatures.addAll(fc.features)
        var maxID = -1
        for (feature in capturedFeatures) {
            feature.intProperty(classIDPropertyKey)?.let { id ->
                if (id > maxID) {
                    maxID = id
                }
            }
        }
        currentID = maxID + 1
    }

    fun processCapturedCRAsAndStoreInCloud(callback: (Result<Unit>) -> Unit) {
        scoped { repo.convertToCSVAndZip(roiDir, roi.name, capturedFeatures).collect{ result ->
            when {
                result.isFailure -> callback(Result.failure(result.exceptionOrNull()!!))
                else -> {
                    val zipFile = result.getOrNull()!!
                    historicalCRA = null
                    val classCountList = capturedCollection.countClasses()
                    val classCounts = ClassCounts(chosenCounts = classCountList)
                    contemporaryCRA = LocalOrRemoteCRAFile(
                        false,
                        null,
                        false,
                        null,
                        zipFile,
                        FieldsCounts(
                            Fields(
                                chosenNumeric = classNumberPropertyKey,
                                chosenString = classNamePropertyKey,
                                chosenStringValues = classCountList.map { it.className },
                            ),
                            StringsNumerics(classCounts, classCounts)
                        )
                    )
                    crasToCloudStorage { result ->
                        if (result.isFailure) {
                            deleteFile(zipFile) {
                                callback(result)
                            }
                        } else {
                            // make a good faith attempt to clean up
                            scoped { repo.deleteCreationDir(roiDir).collect {
                                callback(result)
                            }}
                        }
                    }
                }
            }
        }}
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