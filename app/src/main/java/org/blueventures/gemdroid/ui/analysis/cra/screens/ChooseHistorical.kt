package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Rad

object ChooseHistorical {
    @Composable
    fun Screen(viewModel: CRAViewModel, appBar: AppBarFun, next: Click, back: Click) {
        Nav.Wrap(back, next) { nav ->
            appBar(AppBarUpdate(stringResource(R.string.classification_reference_areas)))
            val choices = viewModel.getHistoricalChoices()

            Col.Col(bottom = 24.dp) {
                Text("Is a historical CRA shapefile available?", fontSize = 24.sp, textAlign = TextAlign.Center)
                Text("(Fields must match contemporary shapefile)", fontSize = 16.sp, textAlign = TextAlign.Center)
                Rad.Io(choices = choices, default = viewModel.historicalChoice, textGetter = { it.label() }, onClick = { choice ->
                    viewModel.historicalChoice = choice
                    nav.next()
                })
            }
        }
    }
}