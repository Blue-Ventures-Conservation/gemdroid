package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Rad

object ChooseHistorical {
    @Composable
    fun Screen(viewModel: CRAViewModel, next: Click) {
        val choices = viewModel.getHistoricalChoices()

        Col.Col(bottom = 24.dp) {
            Text(stringResource(R.string.is_a_historical_cra), fontSize = 24.sp, textAlign = TextAlign.Center)
            Text(stringResource(R.string.fields_must_match), fontSize = 16.sp, textAlign = TextAlign.Center)
            Rad.Io(choices = choices, default = viewModel.historicalChoice, textGetter = { context, choice ->
                context.getString(choice.label())
            }, onClick = { choice ->
                viewModel.historicalChoice = choice
                next()
            })
        }
    }
}