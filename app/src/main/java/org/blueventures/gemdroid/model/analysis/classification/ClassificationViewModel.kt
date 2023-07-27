package org.blueventures.gemdroid.model.analysis.classification

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.analysis.classification.ClassificationROI
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.theme.g2R2B
import java.io.File

class ClassificationViewModel(
    private val repo: ClassificationRepository = ClassificationRepository()
): ApiViewModel(repo) {
    lateinit var sepViewModel: SeparabilityViewModel
    lateinit var craAwaiter: CRAAwaiter

    var roiDir = File("")
        set(value) {
            field = value
            sepViewModel.roiDir = value
            setTileDirs()
        }

    var roi = ROI()
        set(value) {
            field = value
            sepViewModel.roi = value
        }

    fun init(activity: ComponentActivity, awaiter: CRAAwaiter) {
        sepViewModel = activity.viewModels<SeparabilityViewModel>().value
        sepViewModel.init(awaiter)
        craAwaiter = awaiter
    }

    private var classificationJob: Job? = null

    var tileDirs: List<File> = listOf()
    private fun setTileDirs() {
        tileDirs = listOf(repo.contLCTileDir(roiDir), repo.histLCTileDir(roiDir))
    }

    fun getClassification(cra: CRA, callback: (ApiResult<ClassificationURLs>) -> Unit) {
        if (classificationJob != null) return
        apiWithToken({ classificationJob = it }, repo.getClassification(ClassificationROI(
            cra.contemporaryCRA.shapefileStorageKey,
            cra.historicalShp().shapefileStorageKey,
            cra.useContSpec(),
            cra.contemporaryCRA.numericClassField,
            cra.contemporaryCRA.stringClassField,
            makePalette(cra),
            roi,
        ))) { result ->
            classificationJob = null
            callback(result)
        }
    }
    fun saveClassificationFile(urls: ClassificationURLs) = scoped { repo.saveClassificationFile(roiDir, urls).collect() }
    fun loadClassificationFile(callback: (Result<ClassificationURLs>) -> Unit) = scoped { repo.loadClassificationFile(roiDir).collect(callback) }

    private fun makePalette(cra: CRA): List<String> {
        val pal = mutableListOf<String>()
        val size = cra.contemporaryCRA.stringClassValues.size
        for (i in 0 until size) {
            pal.add(g2R2B(i, size))
        }
        return pal
    }
}