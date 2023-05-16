package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.SnackFun

object Name {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBarFun, snack: SnackFun, back: Click, next: Click) {
        appBar(AppBarUpdate(title = "Create Coarse ROI"))

        Col.BigPad {
            Text(text = "Name your ROI", fontSize = 24.sp, textAlign = TextAlign.Center)
            NameField(viewModel = viewModel)
            Butt.Next {
                if (viewModel.notSpecial() && viewModel.isUnique()) {
                    next()
                } else {
                    snack("Please enter a unique name without any special characters.")
                }
            }
        }

        BackHandler(onBack = back)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NameField(viewModel: RoiViewModel) {
        val focus = LocalFocusManager.current
        var text by remember { mutableStateOf(viewModel.name) }

        TextField(
            value = text,
            onValueChange = {  viewModel.name = it; text = it },
            label = { Text("Please enter a name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Words),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            textStyle = TextStyle.Default.copy(fontSize = 24.sp)
        )
    }
}