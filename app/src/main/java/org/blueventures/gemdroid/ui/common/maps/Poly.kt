package org.blueventures.gemdroid.ui.common.maps

import android.content.Context
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.blend3Way

object Poly {
    data class NamedPoly(val name: String, val polygon: MultiPolyPts)
    data class PolygonGroup(val menuTitle: String, val polygons: List<NamedPoly>, val color: Int? = null, val strokeColor: Int = 0x7F000000, val dashes: Boolean = false, val startChecked: Boolean = true)
    data class NamedPolyOptions(val name: String, val options: List<PolygonOptions>)
    data class PolyOptionsGroup(val menuTitle: String, val namedOptions: List<NamedPolyOptions>, val startChecked: Boolean = true)

    abstract class Model {
        abstract val touchEnabled: Boolean
        abstract fun polygonGroups(context: Context, callback: (List<PolygonGroup>) -> Unit): Job
        abstract fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit): Job

        fun polygonOptions(context: Context, callback: (List<PolyOptionsGroup>) -> Unit) {
            polygonGroups(context) { grps ->
                val groups = mutableListOf<PolyOptionsGroup>()

                for (gindex in grps.indices) {
                    val group = grps[gindex]
                    val opts = mutableListOf<NamedPolyOptions>()
                    group.polygons.forEachIndexed { _, poly ->
                        opts.add(NamedPolyOptions(poly.name, PolygonDrawer.opts(poly.polygon, group.color ?: blend3Way(LightGreen, MildRed, SkyBlue, gindex, grps.size, 0x7F), group.strokeColor, group.dashes)))
                    }
                    groups.add(PolyOptionsGroup(group.menuTitle, opts, group.startChecked))
                }

                callback(groups)
            }
        }
    }
}