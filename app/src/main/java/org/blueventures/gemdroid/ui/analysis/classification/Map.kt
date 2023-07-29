package org.blueventures.gemdroid.ui.analysis.classification

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Maps
import org.blueventures.gemdroid.ui.common.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.SnackFun

object Map {
    @StringRes
    private const val title = R.string.classification

    private lateinit var contClassification: Maps.Layer
    private lateinit var histClassification: Maps.Layer

    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, snack: SnackFun, details: Click, back: Click) {
        appBar(AppBarUpdate(title = stringResource(title)))

        Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras_classification), viewModel.craAwaiter) { cra ->
            Classify(viewModel, cra, appBar, details)
        }

        BackHandler(onBack = back)
    }

    @Composable
    fun Classify(viewModel: ClassificationViewModel, cra: CRA, appBar: AppBarFun, details: Click) {
        Maps.LayerSetup({ layers ->
            contClassification = layers[0]
            histClassification = layers[1]
        }, R.string.contemporary_classification, R.string.historical_classification)
        GetRemote.Save(viewModel::loadClassificationFile, { callback ->
            viewModel.getClassification(cra, callback)
        }, viewModel::saveClassificationFile, Classification::errHandler) { urls ->
            viewModel.urls = urls
            ClassificationMap(viewModel, cra, urls, appBar, details)
        }
    }

    @Composable
    fun ClassificationMap(viewModel: ClassificationViewModel, cra: CRA, classificationURLs: ClassificationURLs, appBar: AppBarFun, details: Click) {
        Maps.Screen(stringResource(title), classificationURLs, viewModel.roiDir, appBar, staleCheck = { urls ->
            staleCheck(urls.createdAt, urls.timeout)
        }, tileDir = { i ->
            viewModel.tileDirs[i]
        }, url = { i, urls ->
            urls.ordered(i)
        }, getRemote = { callback ->
            viewModel.getClassification(cra, callback)
        }, viewModel::saveClassificationFile, floatingContent = {
            MapActionButton(details) { Icon(Icons.Filled.Info, stringResource(R.string.view_classifications_details)) }
        }, viewModel.roi.polygon.coordinates[0], contClassification, histClassification)
    }
}