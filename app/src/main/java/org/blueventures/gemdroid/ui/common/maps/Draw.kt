package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Draw {
    interface UI {
        val snack: SnackFun
        val next: Click
    }

    interface Data {
        val maxPoints: Int
        fun points(): MutableList<LatLng>
        fun clear()
        fun polygonOptions(): PolygonOptions?
        fun addPoint(point: LatLng, callback: (Int?) -> Unit): Job
        fun removePrev(callback: (Int?) -> Unit): Job

        /** validation funcs */
        fun validatePolygon(): Boolean
        fun maxSquareKms(): String
        fun polygonSquareKms(): String
        fun area(): Double
    }

    data class Model(private val drawPoly: PolygonDrawer, override val snack: SnackFun, override val next: Click): Data by drawPoly, UI
}