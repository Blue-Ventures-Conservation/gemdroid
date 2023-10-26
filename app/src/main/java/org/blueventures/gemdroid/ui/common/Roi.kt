package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.data.roi.ROI

object Roi {
    @Composable
    fun Loader(getter: ((Result<ROI>?) -> Unit) -> Unit, snack: SnackFun, fail: Click, setter: @Composable (ROI) -> Unit) {
        val (roi, setRoi) = remember { mutableStateOf<Result<ROI>?>(null) }

        when {
            roi == null -> {
                Progress()
                getter(setRoi)
            }
            roi.isFailure -> {
                Progress()
                val ctx = LocalContext.current
                Effect.Once {
                    snack(roi.exceptionOrNull()!!.localized(ctx))
                    fail()
                }
            }
            else -> {
                setter(roi.getOrNull()!!)
            }
        }
    }
}