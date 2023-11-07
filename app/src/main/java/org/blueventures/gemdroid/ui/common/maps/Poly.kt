package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.blend

object Poly {
    abstract class Model {
        abstract val menuTitle: String
        abstract val touchEnabled: Boolean
        abstract val labels: List<String>
        abstract fun polygons(callback: (List<List<List<LatLng>>>) -> Unit)
        abstract fun <T> markerWork(work: () -> T, callback: (T) -> Unit): Job

        fun polygonOptions(callback: (List<PolygonOptions>) -> Unit) {
            polygons { polys ->
                val size = polys.size
                val opts = mutableListOf<PolygonOptions>()
                polys.forEachIndexed { index, poly ->
                    PolygonDrawer.opts(poly, fill = blend(MildRed, SkyBlue, index, size, 0x7F))?.let { opts.add(it) }
                }

                callback(opts)
            }
        }
    }
}