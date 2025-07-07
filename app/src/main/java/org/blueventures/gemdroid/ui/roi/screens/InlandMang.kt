package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info

object InlandMang {
    @Composable
    fun Screen(viewModel: RoiViewModel, appBar: AppBar, next: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.create_project)))

        val (checked, setChecked) = remember { mutableStateOf(false) }

        Col.MidPad {
            Info.Txt(text = stringResource(R.string.are_there_rivers_inland))
            Info.Row {
                Spacer(modifier = Modifier.height(0.dp))
                Info.Txt(text = stringResource(R.string.yes_optional))
                Checkbox(checked = checked, onCheckedChange = setChecked)
                Spacer(modifier = Modifier.height(0.dp))
            }
            Butt.Next {
                viewModel.inlandMang = checked
                next()
            }
        }
    }
}