package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.data.analysis.cra.CRA
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter

object Await {
    @Composable
    fun CRA(
        snack: SnackFun,
        back: Click,
        notVerified: String,
        awaiter: CRAAwaiter,
        content: @Composable (CRA) -> Unit,
    ) {
        val (cras, setCRAs) = remember { mutableStateOf<Result<CRA>?>(null) }
        val (should, setShould) = remember { mutableStateOf<Result<Boolean>?>(null) }
        val (awaited, setAwaited) = remember { mutableStateOf<Result<Unit>?>(null) }

        when {
            cras == null -> {
                awaiter.loadCRAs(setCRAs)
            }
            cras.isFailure -> {
                val msg = cras.exceptionOrNull()!!.localized(LocalContext.current)
                Effect.Once {
                    snack(msg)
                    back()
                }
            }
            should == null -> {
                awaiter.shouldAwaitCRAs(setShould)
            }
            (should.isFailure || should.getOrNull()!!) && awaited == null -> {
                PleaseWait()
                awaiter.awaitCRAs(cras.getOrNull()!!, setAwaited)
            }
            else -> {
                if (awaited != null && (awaited.isFailure || awaited.getOrNull() != null)) {
                    Effect.Once {
                        snack(notVerified)
                    }
                }

                content(cras.getOrNull()!!)
            }
        }
    }
}