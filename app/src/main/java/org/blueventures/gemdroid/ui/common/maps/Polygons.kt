package org.blueventures.gemdroid.ui.common.maps

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.ui.common.maps.Maps.Checker
import org.blueventures.gemdroid.ui.theme.LightGreen
import org.blueventures.gemdroid.ui.theme.SkyBlue
import org.blueventures.gemdroid.ui.theme.blend

object Polygons {
    data class Named(val name: String, val polygon: MultiPolyPts)
    data class Group(val menuTitle: String, val polygons: List<Named>, val color: Int? = null, val strokeColor: Int = 0x7F000000, val dashes: Boolean = false, val startVisible: Boolean = true)
    data class NamedOptions(val name: String, val options: List<PolygonOptions>)
    data class NamedOptionsGroup(val menuTitle: String, val namedOptions: List<NamedOptions>, val startVisible: Boolean = true)

    abstract class Model {
        abstract val touchEnabled: Boolean
        abstract fun polygonGroups(context: Context, callback: (List<Group>) -> Unit): Job
        abstract fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit): Job

        fun polygonOptions(context: Context, callback: (List<NamedOptionsGroup>) -> Unit) {
            polygonGroups(context) { polyGroups ->
                val optGroups = mutableListOf<NamedOptionsGroup>()

                var groupOffset = 0
                for (groupIndex in polyGroups.indices) {
                    val group = polyGroups[groupIndex]
                    val opts = mutableListOf<NamedOptions>()

                    val groupColor = if (group.color == null) {
                        blend(SkyBlue, LightGreen, groupIndex - groupOffset, polyGroups.size, 0x7F)
                    } else {
                        groupOffset += 1
                        ColorUtils.setAlphaComponent(group.color, 0x7F)
                    }
                    group.polygons.forEach { poly ->
                        PolygonUtils.opts(poly.polygon, groupColor, group.strokeColor, dashes = group.dashes)?.let { opt ->
                            opts.add(NamedOptions(poly.name, opt))
                        }
                    }
                    optGroups.add(NamedOptionsGroup(group.menuTitle, opts, group.startVisible))
                }

                callback(optGroups)
            }
        }
    }

    @Composable
    @GoogleMapComposable
    fun Display(model: Model, groups: List<NamedOptionsGroup>, checkers: List<Checker>, lastTouch: MutableState<LatLng?>) {
        val firstZ = 9999f

        val visibilities = mutableListOf<Boolean>()
        for (index in groups.indices) {
            val group = groups[index]
            val zIndex = firstZ - index

            var checker: Checker? = null
            for (c in checkers) {
                if (c.name == group.menuTitle) {
                    checker = c
                    break
                }
            }

            checker?.let {
                visibilities.add(it.state)
                ShowPolygons(checker, zIndex, group)
            }
        }

        if (model.touchEnabled) {
            Touch(model, groups, visibilities, lastTouch)
        }
    }

    @Composable
    @GoogleMapComposable
    private fun ShowPolygons(checker: Checker, zIndex: Float, group: NamedOptionsGroup) {
        val (visible, setVisible) = remember { mutableStateOf(group.startVisible) }
        checker.state = visible
        checker.setState = setVisible

        if (group.namedOptions.isNotEmpty()) {
            for (namedOptions in group.namedOptions) {
                for (opts in namedOptions.options) {
                    Polygon(points = opts.points, fillColor = Color(opts.fillColor), strokeColor = Color(opts.strokeColor), strokePattern = opts.strokePattern, strokeWidth = opts.strokeWidth, visible = checker.state, zIndex = zIndex)
                }
            }
        }
    }

    fun checkers(context: Context, model: Model, callback: (Pair<MutableList<Checker>, List<NamedOptionsGroup>>) -> Unit) {
        model.polygonOptions(context) { groups ->
            val checkers = mutableListOf<Checker>()
            for (group in groups) {
                checkers.add(Checker(group.menuTitle))
            }
            callback(Pair(checkers, groups))
        }
    }

    @Composable
    @GoogleMapComposable
    fun Touch(model: Model, optGroups: List<NamedOptionsGroup>, visibility: List<Boolean>, lastTouch: MutableState<LatLng?>) {
        var markerOpts by remember { mutableStateOf<MarkerOptions?>(null) }
        markerOpts?.let {
            val state by remember { mutableStateOf(MarkerState(it.position)) }
            MarkerInfoWindow(state = state, title = it.title)
        }

        lastTouch.value?.let { pt ->
            if (markerOpts?.position == pt) {
                lastTouch.value = null
            } else {
                model.markerWork({
                    var markerOpts: MarkerOptions? = null
                    for (i in optGroups.indices) {
                        val optGroup = optGroups[i]

                        if (optGroup.namedOptions.isNotEmpty() && visibility[i]) {
                            for (namedOpts in optGroup.namedOptions) {
                                var contained = false
                                for (opts in namedOpts.options) {
                                    if (PolyUtil.containsLocation(pt, opts.points, true)) {
                                        markerOpts = MarkerOptions().position(pt).title(namedOpts.name)
                                        contained = true
                                        break
                                    }
                                }

                                if (contained) {
                                    break
                                }
                            }
                        }

                        if (markerOpts != null) {
                            break
                        }
                    }

                    markerOpts
                }) { opts ->
                    if (opts == null) {
                        lastTouch.value = null
                    }
                    markerOpts = opts
                }
            }
        }
    }
}