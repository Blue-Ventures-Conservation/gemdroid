package org.blueventures.gemdroid.ui.analysis.classification.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.g2R2B

object Details {
    @Composable
    fun Screen(viewModel: ClassificationViewModel, appBar: AppBarFun, separability: Click, back: Click) {
        appBar(AppBarUpdate(title = stringResource(R.string.classification_details)))

        Col.MidPad(arrange = Arrangement.SpaceAround, fill = false, scroll = true) {
            DetailsHeader(stringResource(R.string.legend))
            Legend(viewModel.urls)
            DetailsHeader(stringResource(R.string.accuracy))
            Accuracy(viewModel.urls)
            DetailsHeader(stringResource(R.string.separability))
            Separability(separability)
        }

        BackHandler(onBack = back)
    }

    @Composable
    private fun DetailsHeader(title: String) {
        Column {
            Text(text = title, fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
            Divider(color = SkyBlue, thickness = 1.dp)
        }
    }

    @Composable
    private fun DetailsBlock(content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = Modifier.padding(bottom = 16.dp), content = content)
    }

    @Composable
    private fun Legend(urls: ClassificationURLs) {
        val size = urls.classes.size
        DetailsBlock {
            urls.classes.forEachIndexed { i, clz ->
                LegendRow(clz, g2R2B(i, size))
            }
        }
    }

    @Composable
    private fun LegendRow(clz: String, color: Color) {
        DetailsRow {
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

        DetailsBlock {
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
        DetailsRow {
            Text(text = label, fontSize = 20.sp)
            Text(text = "%.4f".format(accuracy), fontSize = 20.sp)
        }
    }

    @Composable
    private fun Separability(separability: Click) {
        DetailsBlock {
            Spacer(modifier = Modifier.height(16.dp))
            Col.DashboardButton(stringResource(R.string.explore), separability)
        }
    }

    @Composable
    private fun DetailsRow(click: Click = {}, content: @Composable RowScope.() -> Unit) {
        Row(modifier = Modifier
            .clickable(onClick = click)
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically)
        {
            content()
        }
    }
}