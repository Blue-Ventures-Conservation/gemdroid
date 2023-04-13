package org.blueventures.gemdroid.ui.roi

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click

object Indices {
    @Composable
    fun Screen(viewModel: RoiViewModel, back: Click, next: Click) {
        val choices = viewModel.getIndices()
        val (choice, setChoice) = remember { mutableStateOf(viewModel.indices) }

        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Spectral Indices", fontSize = 32.sp)
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                choices.forEach { indices ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (indices == choice),
                                onClick = {
                                    setChoice(indices)
                                }
                            )
                            .padding(top = 24.dp, bottom = 24.dp)
                    ) {
                        RadioButton(selected = (indices == choice), onClick = { setChoice(indices) })
                        Text(text = indices.label, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
            Button(
                onClick = {
                    viewModel.indices = choice
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