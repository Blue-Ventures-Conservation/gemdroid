package org.blueventures.gemdroid.ui.analysis.classification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.StaleCheck
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Tiles

object Map {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, snack: SnackFun, details: Click, back: Click) {
        Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras_classification), viewModel.craAwaiter) { cra ->
            Classify(viewModel, cra, appBar, details, back)
        }
    }

    @Composable
    fun Classify(viewModel: ClassificationViewModel, cra: CRA, appBar: AppBarFun, details: Click, back: Click) {
        Tiles.LayerSetup(R.string.contemporary_classification, R.string.historical_classification)

        GetRemote.Save(viewModel::loadClassificationFile, { callback ->
            viewModel.getClassification(cra, callback)
        }, viewModel::saveClassificationFile, Classification::errHandler) { urls ->
            viewModel.urls = urls

            Maps.Screen(stringResource(R.string.classification), appBar, back = back, floatingContent = {
                MapActionButton(details) { Icon(Icons.Filled.Info, stringResource(R.string.view_classifications_details)) }
            }, tiles = object : Tiles.Model<ClassificationURLs>() {
                override val initUrls: ClassificationURLs = urls
                override val parentDir = viewModel.roiDir
                override val bounds = viewModel.roi.polygon.coordinates[0].map { LatLng(it[1], it[0]) }

                override fun url(i: Int, urls: ClassificationURLs) = urls.ordered(i)
                override fun tileDir(i: Int) = viewModel.tileDirs[i]
                override fun staleCheck(urls: ClassificationURLs) = StaleCheck(urls.createdAt, urls.timeout)
                override fun getRemote(callback: (ApiResult<ClassificationURLs>) -> Unit) = viewModel.getClassification(cra, callback)
                override fun save(urls: ClassificationURLs) = viewModel.saveClassificationFile(urls)
            })
        }
    }
}