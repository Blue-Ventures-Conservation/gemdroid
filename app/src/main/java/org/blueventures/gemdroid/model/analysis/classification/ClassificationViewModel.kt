package org.blueventures.gemdroid.model.analysis.classification

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.analysis.BVClassColors.makeColorPalette
import org.blueventures.gemdroid.data.analysis.Tasks
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.analysis.classification.ClassificationExports
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.data.analysis.classification.ClassificationReady
import org.blueventures.gemdroid.data.analysis.classification.ClassificationReadyResponse
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.cra.ContemporaryAndHistoricalRemoteCRAFileInfo
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.AnalysisDatasource
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.contLCTileDir
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.exportsFile
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.histLCTileDir
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.readyFile
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.resultsFile
import org.blueventures.gemdroid.model.analysis.classification.ClassificationDatasource.Companion.urlsFile
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.theme.toHexString
import java.io.File

class ClassificationViewModel(
    repo: ClassificationRepository = ClassificationRepository()
): ApiViewModel(repo) {
    lateinit var sepViewModel: SeparabilityViewModel
    lateinit var craAwaiter: Await.CRAAwaiter
    lateinit var cras: ContemporaryAndHistoricalRemoteCRAFileInfo

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

    fun init(activity: ComponentActivity, awaiter: Await.CRAAwaiter) {
        sepViewModel = activity.viewModels<SeparabilityViewModel>().value
        sepViewModel.init(awaiter)
        craAwaiter = awaiter
    }

    private var readyJob: Job? = null
    private var classificationJob: Job? = null
    private var exportsJobs: Job? = null
    private var statusJob: Job? = null
    private var contemporaryUriJob: Job? = null
    private var historicalUriJob: Job? = null

    fun tileDirs() = listOf(contLCTileDir(roiDir), histLCTileDir(roiDir))

    fun loadVisualizeFile(callback: (Result<VisualizeURLs>) -> Unit) = loadFile(AnalysisDatasource.urlsFile(roiDir), VisualizeURLs.Companion, callback)

    private var chotOp: String? = null
    private var clotOp: String? = null
    private var hhotOp: String? = null
    private var hlotOp: String? = null
    fun clearCompositeOps() {
        chotOp = null
        clotOp = null
        hhotOp = null
        hlotOp = null
    }

    fun getClassificationReady(callback: (ApiResult<ClassificationReadyResponse>) -> Unit) {
        if (chotOp == null || clotOp == null || hhotOp == null || hlotOp == null) {
            loadVisualizeFile { res ->
                if (res.isSuccess) {
                    val urls = res.getOrNull()!!
                    chotOp = urls.chotImageOp
                    clotOp = urls.clotImageOp
                    hhotOp = urls.hhotImageOp
                    hlotOp = urls.hlotImageOp
                }
                fetchClassificationReady(callback)
            }
        } else {
            fetchClassificationReady(callback)
        }
    }

    private fun fetchClassificationReady(callback: (ApiResult<ClassificationReadyResponse>) -> Unit) {
        readyJob = getRemote(readyJob, makeClassificationReady(chotOp ?: "", clotOp ?: "", hhotOp ?: "", hlotOp ?: ""), callback) { api, ready ->
            api.classificationReady(ready)
        }
    }
    fun saveClassificationReadyFile(ready: ClassificationReadyResponse, callback: (Result<Unit>) -> Unit) = saveFile(readyFile(roiDir), ready, ClassificationReadyResponse.Companion, callback)
    fun loadClassificationReadyFile(callback: (Result<ClassificationReadyResponse>) -> Unit) = loadFile(readyFile(roiDir), ClassificationReadyResponse.Companion) { result ->
        when {
            result.isSuccess -> {
                val resp = result.getOrNull()!!
                chotOp = resp.chotOp
                clotOp = resp.clotOp
                hhotOp = resp.hhotOp
                hlotOp = resp.hlotOp

                if (!resp.isReady()) {
                    callback(Result.failure(Throwable()))
                } else {
                    callback(result)
                }
            }
            else -> callback(result)
        }
    }

    private fun makeClassificationReady(chotOp: String, clotOp: String, hhotOp: String, hlotOp: String) = ClassificationReady(chotOp, clotOp, hhotOp, hlotOp, roi)

    fun getClassification(callback: (ApiResult<ClassificationURLs>) -> Unit) {
        classificationJob = getRemote(classificationJob, makeClassificationROI(), callback) { api, roi ->
            api.classification(roi)
        }
    }
    fun saveClassificationFile(urls: ClassificationURLs, callback: (Result<Unit>) -> Unit) = saveFile(urlsFile(roiDir), urls, ClassificationURLs.Companion, callback)
    fun loadClassificationFile(callback: (Result<ClassificationURLs>) -> Unit) = loadFile(urlsFile(roiDir), ClassificationURLs.Companion, callback)

    fun saveExports(exports: ClassificationExports, callback: (Result<Unit>) -> Unit) = saveFile(exportsFile(roiDir), exports, ClassificationExports.Companion, callback)
    fun loadExports(callback: (Result<ClassificationExports>) -> Unit) = loadFile(exportsFile(roiDir), ClassificationExports.Companion, callback)
    fun getExports(callback: (ApiResult<ClassificationExports>) -> Unit) {
        exportsJobs = getRemote(exportsJobs, makeClassificationROI(), { result ->
            if (result is ApiResult.Success) {
                deleteFile(resultsFile(roiDir)) { callback(result) }
            } else {
                callback(result)
            }
        }) { api, roi ->
            api.exportClassification(roi)
        }
    }

    private fun makeClassificationROI() = ClassificationROI(
        cras.contemporaryCRA.storageKey(),
        cras.historicalShp().storageKey(),
        cras.useContSpec(),
        cras.contemporaryCRA.numericClassField,
        cras.contemporaryCRA.stringClassField,
        makePalette(cras),
        roi,
    )

    fun saveResults(results: TasksResults, callback: (Result<Unit>) -> Unit) = saveResults(resultsFile(roiDir), results, callback)
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

    private fun makePalette(cras: ContemporaryAndHistoricalRemoteCRAFileInfo): List<String> {
        val colors = makeColorPalette(cras.contemporaryCRA.stringClassValues)
        val pal = mutableListOf<String>()
        for (c in colors) {
            pal.add(c.toHexString())
        }
        return pal
    }
}