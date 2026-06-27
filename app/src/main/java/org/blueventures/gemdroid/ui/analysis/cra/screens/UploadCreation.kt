package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object UploadCreation {
    @Composable
    fun Screen(viewModel: CRAViewModel, snack: SnackFun, back: Click, done: Click) {
        val (doItRequest, setDoItRequest) = remember { mutableStateOf<Unit?>(null) }
        when {
            doItRequest == null -> {
                Col.Col(scroll = true) {
                    Info.Txt(stringResource(R.string.caution_you_won_t_be_able_to_create_again))
                    Spacer(modifier = Modifier.height(32.dp))
                    Button({ setDoItRequest(Unit) }) {
                        Text(stringResource(R.string.save_and_move_on), fontSize = 20.sp)
                    }
                }
            }
            else -> {
                Progress()
                val context = LocalContext.current.applicationContext
                viewModel.processAndSaveCapturedCRAs { result ->
                    when {
                        result.isFailure -> {
                            snack(result.exceptionOrNull()!!.localized(context))
                            back()
                        }
                        else -> done()
                    }
                }
            }
        }
    }
}