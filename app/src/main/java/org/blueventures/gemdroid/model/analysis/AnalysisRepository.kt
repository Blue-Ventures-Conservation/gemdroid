package org.blueventures.gemdroid.model.analysis

import org.blueventures.gemdroid.data.analysis.Buffers
import org.blueventures.gemdroid.data.analysis.ImageryExports
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.data.analysis.VisualizeURLs
import org.blueventures.gemdroid.data.roi.ROI
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class AnalysisRepository(
    private val datasource: AnalysisDatasource = AnalysisDatasource(),
): ApiRepository(datasource) {
    fun getStage(roiDir: File) = goFlow { datasource.getStage(roiDir) }
    fun getROI(roiDir: File) = goFlow { datasource.getROI(roiDir) }
    fun saveBuffersFile(roiDir: File, buffers: Buffers) = goFlow { datasource.saveBuffersFile(roiDir, buffers) }
    fun loadBuffersFile(roiDir: File) = goFlow { datasource.loadBuffersFile(roiDir) }
    fun getBuffers(roi: ROI) = goFlow { datasource.getBuffers(roi) }
    fun saveBuffer(roiDir: File, roi: ROI, buffer: Int) = goFlow { // we resave the roi with the buffer distance attached
        val roiSaved = datasource.saveROI(roiDir, roi)
        if (roiSaved.isFailure) {
            roiSaved
        } else {
            datasource.saveBuffer(roiDir, buffer)
        }
    }
    fun getVisualizeURLs(roi: ROI) = goFlow { datasource.getVisualizeURLs(roi) }
    fun saveVisualizeURLs(roiDir: File, urls: VisualizeURLs) = goFlow { datasource.saveVisualizeURLs(roiDir, urls) }
    fun loadVisualizeURLs(roiDir: File) = goFlow { datasource.loadVisualizeURLs(roiDir) }

    fun getExports(roi: ROI) = goFlow { datasource.getExports(roi) }
    fun saveExports(roiDir: File, exports: ImageryExports) = goFlow { datasource.saveExports(roiDir, exports) }
    fun loadExports(roiDir: File) = goFlow { datasource.loadExports(roiDir) }

    fun loadResults(roiDir: File) = goFlow { datasource.loadResults(roiDir) }
    fun saveResults(roiDir: File, results: TasksResults) = goFlow { datasource.saveResults(roiDir, results) }
    fun deleteResults(roiDir: File) = goFlow { datasource.deleteResults(roiDir) }

    fun chotTileDir(roiDir: File) = datasource.chotTileDir(roiDir)
    fun clotTileDir(roiDir: File) = datasource.clotTileDir(roiDir)
    fun hhotTileDir(roiDir: File) = datasource.hhotTileDir(roiDir)
    fun hlotTileDir(roiDir: File) = datasource.hlotTileDir(roiDir)
}