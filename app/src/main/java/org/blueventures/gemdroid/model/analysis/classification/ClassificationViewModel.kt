package org.blueventures.gemdroid.model.analysis.classification

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.classification.ClassificationExports
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.contLCTileDir
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.exportsFile
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.histLCTileDir
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.resultsFile
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.urlsFile
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.theme.g2R2BHex
import java.io.File

class ClassificationViewModel(
    repo: ClassificationRepository = ClassificationRepository()
): Downloads.VisualizeHolder, ApiViewModel(repo) {
    lateinit var sepViewModel: SeparabilityViewModel
    lateinit var craAwaiter: Await.CRAAwaiter
    lateinit var cra: CRA

    var roiDir = File("")
        set(value) {
            field = value
            sepViewModel.roiDir = value
        }

    var roi = ROI()
        set(value) {
            field = value
            sepViewModel.roi = value
        }

    lateinit var urls: ClassificationURLs
    override var visualize = true

    fun init(activity: ComponentActivity, awaiter: Await.CRAAwaiter) {
        sepViewModel = activity.viewModels<SeparabilityViewModel>().value
        sepViewModel.init(awaiter)
        craAwaiter = awaiter
    }

    private var classificationJob: Job? = null
    private var exportsJobs: Job? = null
    private var statusJob: Job? = null
    private var contemporaryUriJob: Job? = null
    private var historicalUriJob: Job? = null

    fun tileDirs() = listOf(contLCTileDir(roiDir), histLCTileDir(roiDir))

    fun getClassification(callback: (ApiResult<ClassificationURLs>) -> Unit) {
        classificationJob = getRemote(classificationJob, makeClassificationROI(), callback) { api, roi ->
            api.classification(roi)
        }
    }
    fun saveClassificationFile(urls: ClassificationURLs) = saveFile(urlsFile(roiDir), urls, ClassificationURLs.Companion)
    fun loadClassificationFile(callback: (Result<ClassificationURLs>) -> Unit) = loadFile(urlsFile(roiDir), ClassificationURLs.Companion, callback)

    fun saveExports(exports: ClassificationExports) = saveFile(exportsFile(roiDir), exports, ClassificationExports.Companion)
    fun loadExports(callback: (Result<ClassificationExports>) -> Unit) = loadFile(exportsFile(roiDir), ClassificationExports.Companion, callback)
    fun getExports(visualize: Boolean, callback: (ApiResult<ClassificationExports>) -> Unit) {
        exportsJobs = getRemote(exportsJobs, makeClassificationROI(visualize), { result ->
            if (result is ApiResult.Success) {
                deleteFile(resultsFile(roiDir)) { callback(result) }
            } else {
                callback(result)
            }
        }) { api, roi ->
            api.exportClassification(roi)
        }
    }

    private fun makeClassificationROI(visualize: Boolean = true) = ClassificationROI(
        cra.contemporaryCRA.shapefileStorageKey,
        cra.historicalShp().shapefileStorageKey,
        cra.useContSpec(),
        cra.contemporaryCRA.numericClassField,
        cra.contemporaryCRA.stringClassField,
        makePalette(cra),
        roi.copy(visualize = visualize),
    )

    fun saveResults(results: TasksResults) = saveResults(resultsFile(roiDir), results)
    fun loadResults(callback: (Result<TasksResults>) -> Unit) = loadResults(resultsFile(roiDir), callback)
    fun getResults(exports: ClassificationExports, callback: (ApiResult<TasksResults>) -> Unit) {
        statusJob = getRemote(statusJob, Tasks(listOf(exports.contemporary.name, exports.historical.name)), callback) { api, tasks ->
            api.tasksResults(tasks)
        }
    }

    fun getContemporaryUri(path: String, callback: (Result<Uri>) -> Unit) {
        contemporaryUriJob = uriFromStorage(contemporaryUriJob, path, callback)
    }
    fun getHistoricalUri(path: String, callback: (Result<Uri>) -> Unit) {
        historicalUriJob = uriFromStorage(historicalUriJob, path, callback)
    }

    fun clearExports() {
        deleteFile(resultsFile(roiDir))
        deleteFile(exportsFile(roiDir))
    }

    private fun makePalette(cra: CRA): List<String> {
        val pal = mutableListOf<String>()
        val size = cra.contemporaryCRA.stringClassValues.size
        for (i in 0 until size) {
            pal.add(g2R2BHex(i, size))
        }
        return pal
    }
}