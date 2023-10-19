package org.blueventures.gemdroid.ui.analysis.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Downloads
import org.blueventures.gemdroid.ui.common.Nav

object Downloads {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, appBar: AppBar, back: Click) {
        viewModel.visualize = true
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.imagery_downloads))))
            Downloads.Screen(viewModel, getLocal = viewModel::loadExports, getRemote = viewModel::getExports, save = viewModel::saveExports, {
                viewModel.clearExports()
                back()
            }) { exports ->
                val chotTitle = stringResource(R.string.cont_high_tide)
                val clotTitle = stringResource(R.string.cont_low_tide)
                val hhotTitle = stringResource(R.string.hist_high_tide)
                val hlotTitle = stringResource(R.string.hist_low_tide)

                object : Downloads.ExportList {
                    override fun saveResults(results: TasksResults) = viewModel.saveResults(results)
                    override fun loadResults(callback: (Result<TasksResults>) -> Unit) = viewModel.loadResults(callback)
                    override fun getResults(callback: (ApiResult<TasksResults>) -> Unit) = viewModel.getResults(exports, callback)
                    override fun list(): List<Downloads.NamedExport> {
                        return listOf(
                            object : Downloads.NamedExport {
                                override val title = chotTitle
                                override val filename = "${viewModel.roi.name}_contemporary_high_tide.tif"
                                override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getChotUri(exports.chot.storagePath, callback)
                            },
                            object : Downloads.NamedExport {
                                override val title = clotTitle
                                override val filename = "${viewModel.roi.name}_contemporary_low_tide.tif"
                                override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getClotUri(exports.clot.storagePath, callback)
                            },
                            object : Downloads.NamedExport {
                                override val title = hhotTitle
                                override val filename = "${viewModel.roi.name}_historical_high_tide.tif"
                                override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getHhotUri(exports.hhot.storagePath, callback)
                            },
                            object : Downloads.NamedExport {
                                override val title = hlotTitle
                                override val filename = "${viewModel.roi.name}_historical_low_tide.tif"
                                override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getHlotUri(exports.hlot.storagePath, callback)
                            },
                        )
                    }
                }
            }
        }
    }
}