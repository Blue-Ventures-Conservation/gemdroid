package org.blueventures.gemdroid.ui.common.maps

import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.PolyPts
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.MildRed
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.blend3Way

object Poly {
    data class NamedPoly(val name: String, val polygon: PolyPts)
    data class PolygonGroup(val menuTitle: Int, val polygons: List<NamedPoly>, val color: Int? = null, val startChecked: Boolean = true)
    data class NamedPolyOptions(val name: String, val options: PolygonOptions)
    data class PolyOptionsGroup(val menuTitle: Int, val options: List<NamedPolyOptions>, val startChecked: Boolean = true)

    abstract class Model {
        abstract val touchEnabled: Boolean
        abstract fun polygonGroups(callback: (List<PolygonGroup>) -> Unit): Job
        abstract fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit): Job

        fun polygonOptions(callback: (List<PolyOptionsGroup>) -> Unit) {
            polygonGroups { grps ->
                var totalPolys = 0
                val prevCounts = mutableListOf(0)
                for (group in grps) {
                    totalPolys += group.polygons.size
                    prevCounts.add(totalPolys)
                }

                val groups = mutableListOf<PolyOptionsGroup>()

                for (i in grps.indices) {
                    val prevCount = prevCounts[i]
                    val group = grps[i]
                    val opts = mutableListOf<NamedPolyOptions>()
                    group.polygons.forEachIndexed { index, poly ->
                        opts.add(NamedPolyOptions(poly.name, PolygonDrawer.opts(poly.polygon, fill = group.color ?: blend3Way(LightGreen, MildRed, SkyBlue, prevCount + index, totalPolys, 0x7F))))
                    }
                    groups.add(PolyOptionsGroup(group.menuTitle, opts, group.startChecked))
                }

                callback(groups)
            }
        }
    }
}