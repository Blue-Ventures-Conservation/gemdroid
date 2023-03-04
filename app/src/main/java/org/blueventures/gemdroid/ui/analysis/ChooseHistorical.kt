package org.blueventures.gemdroid.ui.analysis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.ui.common.Click

object ChooseHistorical {
    @Composable
    fun Screen(viewModel: AnalysisViewModel, next: Click, back: Click) {
        val choices = viewModel.getHistoricalChoices()
        val (choice, setChoice) = remember { mutableStateOf(viewModel.historicalChoice) }

        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Is a historical CRA shapefile available?", fontSize = 24.sp, textAlign = TextAlign.Center)
            Text("(Fields must match contemporary shapefile)", fontSize = 16.sp, textAlign = TextAlign.Center)
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                choices.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (option == choice),
                                onClick = {
                                    setChoice(option)
                                }
                            )
                            .padding(top = 24.dp, bottom = 24.dp)
                    ) {
                        RadioButton(selected = (option == choice), onClick = { setChoice(option) })
                        Text(text = option.label(), modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
            Button(
                onClick = {
                    viewModel.historicalChoice = choice
                    next()
                }
            ) {
                Text("Next", fontSize = 20.sp)
            }
        }

        BackHandler {
            back()
        }
    }
}