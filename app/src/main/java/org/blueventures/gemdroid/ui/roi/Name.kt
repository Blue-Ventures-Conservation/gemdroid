package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarUpdate

object Name {
    @Composable
    fun Screen(viewModel: RoiViewModel, setAppBarState: (AppBarUpdate) -> Unit, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
        setAppBarState(AppBarUpdate(title = "Create ROI"))

        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Name your ROI", fontSize = 24.sp, textAlign = TextAlign.Center)
            NameField(viewModel = viewModel)
            NameButton {
                val name = viewModel.state.value.name
                if (viewModel.notSpecial(name) && viewModel.isUnique(name)) {
                    viewModel.setName(name)
                    nextClick()
                } else {
                    snackbar("Please enter a unique name without any special characters.")
                }
            }
        }

        BackHandler {
            backClick()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NameField(viewModel: RoiViewModel) {
        val focus = LocalFocusManager.current
        var text by remember { mutableStateOf(viewModel.state.value.name) }

        TextField(
            value = text,
            onValueChange = {  viewModel.setName(it); text = it },
            label = { Text("Please enter a name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Words),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            textStyle = TextStyle.Default.copy(fontSize = 24.sp)
        )
    }

    @Composable
    fun NameButton(nextClick: () -> Unit) {
        Button(
            onClick = { nextClick() }
        ) {
            Text("Next", fontSize = 20.sp)
        }
    }
}