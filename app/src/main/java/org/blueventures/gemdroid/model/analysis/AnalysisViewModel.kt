package org.blueventures.gemdroid.model.analysis

import com.github.zibnix.droidbones.api.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.blueventures.gemdroid.data.Buffers
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.data.VisualizeURLs
import org.blueventures.gemdroid.model.analysis.cra.CraViewModel
import org.blueventures.gemdroid.model.analysis.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File

class AnalysisViewModel(private val repo: AnalysisRepository = AnalysisRepository()): ApiViewModel(repo) {
    lateinit var craViewModel: CraViewModel
    lateinit var sepViewModel: SeparabilityViewModel
    var roiDir = File("")
        set(value) {
            field = value
            craViewModel.roiDir = value
            sepViewModel.roiDir = value
            setTileDirs()
        }

    var roi = ROI()
        set(value) {
            field = value
            sepViewModel.roi = value
        }

    var buffersJob: Job? = null
    var visualizeURLsJob: Job? = null

    var tileDirs: List<File> = listOf()
    private fun setTileDirs() {
        tileDirs = listOf(repo.chotTileDir(roiDir), repo.clotTileDir(roiDir), repo.hhotTileDir(roiDir), repo.hlotTileDir(roiDir))
    }

    fun refreshStage(callback: (Stage) -> Unit) = scoped { repo.getStage(roiDir).collect(callback) }
    fun getROI(callback: (Result<ROI>) -> Unit) = scoped { repo.getROI(roiDir).collect(callback) }

    fun saveBuffersFile(buffers: Buffers) = scoped { repo.saveBuffersFile(roiDir, buffers).collect() }
    fun loadBuffersFile(callback: (Result<Buffers>) -> Unit) = scoped { repo.loadBuffersFile(roiDir).collect(callback) }
    fun getBuffers(callback: (ApiResult<Buffers>) -> Unit) {
        if (buffersJob != null) return
        apiWithToken({ buffersJob = it }, repo.getBuffers(roi)) { result ->
            buffersJob = null
            callback(result)
        }
    }

    fun saveBuffer(buffer: Int, callback: (Result<Unit>) -> Unit) = scoped {
        roi = roi.copy(buffDist = buffer)
        repo.saveBuffer(roiDir, roi, buffer).collect(callback)
    }

    fun saveVisualizeURLsFile(urls: VisualizeURLs) = scoped { repo.saveVisualizeURLs(roiDir, urls).collect() }
    fun loadVisualizeURLsFile(callback: (Result<VisualizeURLs>) -> Unit) = scoped { repo.loadVisualizeURLs(roiDir).collect(callback) }
    fun getVisualizeURLs(callback: (ApiResult<VisualizeURLs>) -> Unit) {
        if (visualizeURLsJob != null) return
        apiWithToken({ visualizeURLsJob = it }, repo.getVisualizeURLs(roi)) { result ->
            visualizeURLsJob = null
            callback(result)
        }
    }

    fun chotTileDir(): File = repo.chotTileDir(roiDir)
    fun clotTileDir(): File = repo.clotTileDir(roiDir)
    fun hhotTileDir(): File = repo.hhotTileDir(roiDir)
    fun hlotTileDir(): File = repo.hlotTileDir(roiDir)
}

