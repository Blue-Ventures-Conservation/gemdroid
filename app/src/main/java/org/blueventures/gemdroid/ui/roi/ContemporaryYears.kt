package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargemap.compose.numberpicker.NumberPicker
import org.blueventures.gemdroid.model.roi.RoiViewModel

object ContemporaryYears {
    @Composable
    fun Screen(viewModel: RoiViewModel, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select bounding years (inclusive) for contemporary imagery:", textAlign = TextAlign.Center, fontSize = 24.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContemporaryYearStart(viewModel)
                ContemporaryYearEnd(viewModel)
            }
            Button(
                onClick = {
                    if (viewModel.validateContemporaryYearsOrder()) {
                        if (viewModel.validateContemporaryYearsGap()) {
                            nextClick()
                        } else {
                            snackbar("Please select years less than ${RoiViewModel.maxYearGap} years apart")
                        }
                    } else {
                        snackbar("Year on the left must be equal to or less than the one on right")
                    }
                }
            ) {
                Text("Next", fontSize = 20.sp)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun ContemporaryYearStart(viewModel: RoiViewModel) {
        var year by remember { mutableStateOf(viewModel.state.value.contemporaryYearStart) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..viewModel.currentYear(),
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setContemporaryYearStart(it)
                year = it
            }
        )
    }

    @Composable
    fun ContemporaryYearEnd(viewModel: RoiViewModel) {
        var year by remember { mutableStateOf(viewModel.state.value.contemporaryYearEnd) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..viewModel.currentYear(),
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setContemporaryYearEnd(it)
                year = it
            }
        )
    }
}