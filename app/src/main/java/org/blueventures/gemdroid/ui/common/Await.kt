package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.zibnix.droidbones.localized
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.CRA

object Await {
    interface CRAAwaiter {
        fun loadCRAs(callback: (Result<CRA>) -> Unit): Job
        fun shouldAwaitCRAs(callback: (Result<Boolean>) -> Unit): Job
        fun awaitCRAs(cra: CRA, callback: (Result<Unit>) -> Unit)
    }

    @Composable
    fun CRAOrGoBack(
        snack: SnackFun,
        back: Click,
        notVerified: String,
        awaiter: CRAAwaiter,
        content: @Composable (CRA) -> Unit,
    ) {
        CRA(notVerified, awaiter) { cra, msg ->
            msg?.let {
                snack.once(it)
            }

            cra?.let {
                content(it)
            } ?: run {
                back.once()
            }
        }
    }

    @Composable
    fun CRA(
        notVerified: String,
        awaiter: CRAAwaiter,
        content: @Composable (CRA?, String?) -> Unit,
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
                content(null, msg)
            }
            should == null -> {
                awaiter.shouldAwaitCRAs(setShould)
            }
            (should.isFailure || should.getOrNull()!!) && awaited == null -> {
                PleaseWait()
                awaiter.awaitCRAs(cras.getOrNull()!!, setAwaited)
            }
            else -> {
                var msg: String? = null
                if (awaited != null && awaited.isFailure) {
                    msg = notVerified
                }

                content(cras.getOrNull()!!, msg)
            }
        }
    }
}