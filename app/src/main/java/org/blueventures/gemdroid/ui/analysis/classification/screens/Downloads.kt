package org.blueventures.gemdroid.ui.analysis.classification.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.common.Nav

object Downloads {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, back: Click) {
        viewModel.visualize = true
        Nav.Wrap(back) { nav ->
            appBar(AppBarUpdate(viewModel.roi.appBar(stringResource(R.string.classification_downloads))))
            Downloads.Screen(viewModel, getLocal = viewModel::loadExports, getRemote = viewModel::getExports, save = viewModel::saveExports, {
                viewModel.clearExports()
                nav.back()
            }) { exports ->
                val contemporaryTitle = stringResource(R.string.contemporary_classification)
                val historicalTitle = stringResource(R.string.historical_classification)

                object : Downloads.ExportList {
                    override fun saveResults(results: TasksResults) = viewModel.saveResults(results)
                    override fun loadResults(callback: (Result<TasksResults>) -> Unit) = viewModel.loadResults(callback)
                    override fun getResults(callback: (ApiResult<TasksResults>) -> Unit) = viewModel.getResults(exports, callback)
                    override fun list(): List<Downloads.NamedExport> {
                        return listOf(
                            object : Downloads.NamedExport {
                                override val title = contemporaryTitle
                                override val filename = "${viewModel.roi.name}_contemporary_classification.tif"
                                override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getContemporaryUri(exports.contemporary.storagePath, callback)
                            },
                            object : Downloads.NamedExport {
                                override val title = historicalTitle
                                override val filename = "${viewModel.roi.name}_historical_classification.tif"
                                override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getHistoricalUri(exports.historical.storagePath, callback)
                            }
                        )
                    }
                }
            }
        }
    }
}