package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.TasksResults
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Downloads

object Downloads {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, back: Click) {
        appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics_downloads))))
        Downloads.Screen(null, getLocal = viewModel::loadExports, getRemote = viewModel::getExports, save = viewModel::saveExports, {
            viewModel.clearExports()
            back()
        }) { exports ->
            val lossTitle = stringResource(R.string.loss)
            val persistenceTitle = stringResource(R.string.persistence)
            val gainTitle = stringResource(R.string.gain)

            object : Downloads.ExportList {
                override fun saveResults(results: TasksResults) = viewModel.saveResults(results)
                override fun loadResults(callback: (Result<TasksResults>) -> Unit) = viewModel.loadResults(callback)
                override fun getResults(callback: (ApiResult<TasksResults>) -> Unit) = viewModel.getResults(exports, callback)
                override fun list(): List<Downloads.NamedExport> {
                    return listOf(
                        object : Downloads.NamedExport {
                            override val title = lossTitle
                            override val filename = "${viewModel.roi.name}_loss_dynamics.tif"
                            override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getLossUri(exports.loss.storagePath, callback)
                        },
                        object : Downloads.NamedExport {
                            override val title = persistenceTitle
                            override val filename = "${viewModel.roi.name}_persistence_dynamics.tif"
                            override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getPersistenceUri(exports.persistence.storagePath, callback)
                        },
                        object : Downloads.NamedExport {
                            override val title = gainTitle
                            override val filename = "${viewModel.roi.name}_gain_dynamics.tif"
                            override fun getDownloadUri(callback: (Result<Uri>) -> Unit) = viewModel.getGainUri(exports.gain.storagePath, callback)
                        }
                    )
                }
            }
        }
    }
}