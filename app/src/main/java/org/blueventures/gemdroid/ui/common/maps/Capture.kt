package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Capture {
    interface UI {
        val snack: SnackFun
        val next: Click
    }

    interface Data {
        fun capture(polygon: List<LatLng>)
    }

    data class Model(private val data: Data, override val snack: SnackFun, override val next: Click): Data by data, UI
}