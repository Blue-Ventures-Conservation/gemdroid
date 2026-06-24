package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow
import org.blueventures.gemdroid.data.Rectangle
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Capture {
    interface UI {
        val snack: SnackFun
        val done: Click
    }

    interface Data {
        val scale: Double
        val classes: List<BVClass>
        val shapes: StateFlow<Rectangle>

        fun capture(craClass: BVClass, polygon: List<LatLng>)
    }

    data class Model(private val data: Data, override val snack: SnackFun, override val done: Click): Data by data, UI
}