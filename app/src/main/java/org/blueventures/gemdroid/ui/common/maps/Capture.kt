package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Capture {
    interface UI {
        val snack: SnackFun
        val next: Click
    }

    interface Data {
        val classes: List<BVClass>
        val shapes: List<PolygonDrawer.Rectangle>
        fun capture(craClass: BVClass, polygon: List<LatLng>)
    }

    data class Model(private val data: Data, override val snack: SnackFun, override val next: Click): Data by data, UI
}