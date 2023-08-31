package org.blueventures.gemdroid.ui.analysis.classification.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.theme.g2R2B

object Details {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, separability: Click, back: Click) {
        Nav.Wrap(back, separability) {
            appBar(AppBarUpdate(viewModel.roi.appBar(stringResource(R.string.classification))))

            Col.MidPad(arrange = Arrangement.Top, scroll = true) {
                Legend(viewModel.urls)
                Accuracy(viewModel.urls)
                Separability(separability)
            }
        }
    }

    @Composable
    private fun Legend(urls: ClassificationURLs) {
        val size = urls.classes.size
        Info.Block {
            Info.Header(stringResource(R.string.legend))
            urls.classes.forEachIndexed { i, clz ->
                LegendRow(clz, g2R2B(i, size))
            }
        }
    }

    @Composable
    private fun LegendRow(clz: String, color: Color) {
        Info.Row {
            Info.Txt(clz)
            Box(modifier = Modifier
                .background(color)
                .size(24.dp))
        }
    }

    @Composable
    private fun Accuracy(urls: ClassificationURLs) {
        val resubLabel = stringResource(R.string.resubstitution)
        val validLabel = stringResource(R.string.validation)

        Info.Block {
            Info.Header(stringResource(R.string.accuracy))
            Info.SubHeader(stringResource(R.string.contemporary_accuracy))
            AccuracyRow(resubLabel, urls.contemporaryClassification.resubstitutionAccuracy)
            AccuracyRow(validLabel, urls.contemporaryClassification.validationAccuracy)
            Info.SubHeader(stringResource(R.string.historical_accuracy))
            AccuracyRow(resubLabel, urls.historicalClassification.resubstitutionAccuracy)
            AccuracyRow(validLabel, urls.historicalClassification.validationAccuracy)
        }
    }

    @Composable
    private fun AccuracyRow(label: String, accuracy: Float) {
        Info.Row {
            Info.Txt(label)
            Info.Txt("%.4f".format(accuracy))
        }
    }

    @Composable
    private fun Separability(separability: Click) {
        Info.Block {
            Info.Header(stringResource(R.string.separability))
            Info.Space()
            Col.DashboardButton(stringResource(R.string.explore), separability)
        }
    }
}