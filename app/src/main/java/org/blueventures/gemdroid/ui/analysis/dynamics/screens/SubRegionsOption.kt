package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.analysis.dynamics.Dynamics
import org.blueventures.gemdroid.ui.common.Await
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.once

object SubRegionsOption {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, snack: SnackFun, back: Click, content: @Composable () -> Unit) {
        Await.CRA(snack, back, stringResource(R.string.could_not_verify_cras).format(stringResource(R.string.dynamics)), viewModel.craAwaiter) { cra ->
            viewModel.cra = cra
            val (classificationURLs, setClassificationURLs) = remember { mutableStateOf<Result<ClassificationURLs>?>(null) }

            when {
                classificationURLs == null -> viewModel.loadClassificationFile(setClassificationURLs)
                classificationURLs.isFailure -> {
                    snack.once(classificationURLs.exceptionOrNull()!!.localized(LocalContext.current))
                    back.once()
                }
                else -> {
                    val urls = classificationURLs.getOrNull()!!
                    val contOp = urls.contemporaryClassification.imageOp
                    val histOp = urls.historicalClassification.imageOp

                    GetRemote.Save(
                        getLocal = viewModel::loadDynamicsReadyFile,
                        getRemote = { callback ->
                            viewModel.getDynamicsReady(contOp, histOp, callback)
                        },
                        save = viewModel::saveDynamicsReadyFile,
                        errorHandler = Dynamics::errHandler
                    ) { resp ->
                        when {
                            resp.isReady() -> content()
                            else -> {
                                snack.once(stringResource(R.string.please_wait_the_classification_is_being_saved))
                                back.once()
                            }
                        }
                    }
                }
            }
        }
    }
}