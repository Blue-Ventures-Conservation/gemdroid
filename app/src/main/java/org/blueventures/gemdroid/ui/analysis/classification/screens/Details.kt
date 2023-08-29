package org.blueventures.gemdroid.ui.analysis.classification.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.ClassificationURLs
import org.blueventures.gemdroid.model.analysis.classification.ClassificationViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.g2R2B

object Details {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, separability: Click, back: Click) {
        Nav.Wrap(back, separability) { sep ->
            appBar(AppBarUpdate(title = stringResource(R.string.classification_details)))

            Col.MidPad(arrange = Arrangement.SpaceAround, fill = false, scroll = true) {
                Info.Header(stringResource(R.string.legend))
                Legend(viewModel.urls)
                Info.Header(stringResource(R.string.accuracy))
                Accuracy(viewModel.urls)
                Info.Header(stringResource(R.string.separability))
                Separability(separability)
            }
        }
    }

    @Composable
    private fun Legend(urls: ClassificationURLs) {
        val size = urls.classes.size
        Info.Block {
            urls.classes.forEachIndexed { i, clz ->
                LegendRow(clz, g2R2B(i, size))
            }
        }
    }

    @Composable
    private fun LegendRow(clz: String, color: Color) {
        Info.Row {
            Text(text = clz, fontSize = 20.sp)
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
            AccuracyRowLabel(stringResource(R.string.contemporary_accuracy))
            AccuracyRow(resubLabel, urls.contemporaryClassification.resubstitutionAccuracy)
            AccuracyRow(validLabel, urls.contemporaryClassification.validationAccuracy)
            AccuracyRowLabel(stringResource(R.string.historical_accuracy))
            AccuracyRow(resubLabel, urls.historicalClassification.resubstitutionAccuracy)
            AccuracyRow(validLabel, urls.historicalClassification.validationAccuracy)
        }
    }

    @Composable
    private fun AccuracyRowLabel(title: String) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp)) {
            Text(text = title, fontSize = 20.sp, textAlign = TextAlign.Center)
            Divider(color = SkyBlue, thickness = 1.dp)
        }
    }

    @Composable
    private fun AccuracyRow(label: String, accuracy: Float) {
        Info.Row {
            Text(text = label, fontSize = 20.sp)
            Text(text = "%.4f".format(accuracy), fontSize = 20.sp)
        }
    }

    @Composable
    private fun Separability(separability: Click) {
        Info.Block {
            Spacer(modifier = Modifier.height(16.dp))
            Col.DashboardButton(stringResource(R.string.explore), separability)
        }
    }
}