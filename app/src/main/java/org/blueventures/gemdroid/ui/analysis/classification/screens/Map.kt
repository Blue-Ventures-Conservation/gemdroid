package org.blueventures.gemdroid.ui.analysis.classification.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Tiles

object Map {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, snack: SnackFun, details: Click, back: Click) {
        Nav.Wrap(back, details) { nav ->
            Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras_classification), viewModel.craAwaiter) { cra ->
                viewModel.cra = cra
                Classify(viewModel, appBar) {
                    nav.next()
                }
            }
        }
    }

    @Composable
    fun Classify(viewModel: ClassificationViewModel, appBar: AppBarFun, details: Click) {
        GetRemote.Save(viewModel::loadClassificationFile, viewModel::getClassification, viewModel::saveClassificationFile, Classification::errHandler) { urls ->
            viewModel.urls = urls

            Maps.Screen(floatingContent = {
                MapActionButton(details) { Icon(Icons.Filled.Info, stringResource(R.string.view_classifications_details)) }
            }, tiles = object : Tiles.Model<ClassificationURLs>() {
                override val title = stringResource(R.string.classification)
                override val appBar = appBar
                override val initUrls = urls
                override val layerNames = stringArrayResource(R.array.classification_layers).toList()
                override val parentDir = viewModel.roiDir
                override val bounds = viewModel.roi.bounds()

                override fun tileDir(i: Int) = viewModel.tileDirs()[i]
                override fun getRemote(callback: (ApiResult<ClassificationURLs>) -> Unit) = viewModel.getClassification(callback)
                override fun save(urls: ClassificationURLs) = viewModel.saveClassificationFile(urls)
            })
        }
    }
}