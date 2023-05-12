package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Rad

object Indices {
    @Composable
    fun Screen(viewModel: RoiViewModel, back: Click, next: Click) {
        val choices = viewModel.getIndices()

        Col.BigPad {
            Text("Spectral Indices", fontSize = 32.sp)
            Rad.Io(choices = choices, default = viewModel.indices, textGetter = { it.label }, onClick = { choice ->
                    viewModel.indices = choice
                    next()
                }
            )
        }

        BackHandler {
            back()
        }
    }
}