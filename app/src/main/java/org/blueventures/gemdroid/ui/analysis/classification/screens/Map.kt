package org.blueventures.gemdroid.ui.analysis.classification.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.analysis.classification.Classification
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Layers
import org.blueventures.gemdroid.ui.common.maps.Maps

object Map {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBar, snack: SnackFun, back: Click, details: Click) {
        Await.CRAOrGoBack(snack, back, stringResource(R.string.could_not_verify_cras_classification), viewModel.craAwaiter) { cras ->
            viewModel.cras = cras
            Classify(viewModel, appBar, details)
        }
    }

    @Composable
    fun Classify(viewModel: ClassificationViewModel, appBar: AppBar, details: Click) {
        GetRemote.Save(viewModel::loadClassificationFile, viewModel::getClassification, viewModel::saveClassificationFile, errorHandler = Classification::errHandler) { urls ->
            viewModel.urls = urls

            Maps.Screen(appBar, viewModel.roi.appBarTitle(stringResource(R.string.classification)), storage = Maps.Storage.fromViewModel(viewModel), center = PolygonUtils.centerFromMultiPoly(viewModel.roi.boundaryPolyToState()), layers = object : Layers.Model<ClassificationURLs>() {
                override val initUrls = urls
                override val layerNames = stringArrayResource(R.array.classification_layers).toList()
                override val parentDir = viewModel.roiDir

                override fun tileDir(i: Int) = viewModel.tileDirs()[i]
                override fun getRemote(callback: (ApiResult<ClassificationURLs>) -> Unit) = viewModel.getClassification(callback)
                override fun save(urls: ClassificationURLs) = viewModel.saveClassificationFile(urls)
            }, next = Maps.FloatingNext(Icons.Filled.TableChart, R.string.view_classifications_details, details))
        }
    }
}