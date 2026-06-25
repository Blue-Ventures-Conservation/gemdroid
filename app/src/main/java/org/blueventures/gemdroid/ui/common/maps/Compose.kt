package org.blueventures.gemdroid.ui.common.maps

import android.graphics.Point
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.FlowPreview
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds.centerFromRing
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.maps.Draw.DrawButton
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton

object Compose {
    data class Checker(val name: String, var state: Boolean = true, var setState: (Boolean) -> Unit = {})

    @Composable
    fun <T : URLs> Screen(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        initialZoom: Float?,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        DrawingControls(appBar, title, gps, center, initialZoom, storage, layers, draw, poly, capture, floating)
    }

    @Composable
    private fun <T : URLs> DrawingControls(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        initialZoom: Float?,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val clearState = remember { mutableStateOf(false) }
            val drawState = remember { mutableStateOf(false) }
            draw?.let { draw ->
                Info.Row {
                    Butt.Text(stringResource(R.string.clear)) {
                        clearState.value = true
                        drawState.value = false
                    }
                    val pleaseDraw = stringResource(R.string.please_create_polygon)
                    val tooBig = stringResource(R.string.polygon_sizing)
                    Butt.Next {
                        if (draw.validatePolygon()) {
                            draw.next()
                        } else {
                            val msg = if (draw.area() <= 0) {
                                pleaseDraw
                            } else {
                                val max = draw.maxHectares()
                                val current = draw.polygonHectares()
                                tooBig.format(max, current)
                            }

                            draw.snack(msg)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val touchState = remember { mutableStateOf<LatLng?>(null) }
                var screenPoints by remember { mutableStateOf(listOf<Point>()) }

                Zoom(appBar, title, gps, center, initialZoom, storage, layers, draw, poly, capture, clearState, touchState, screenPoints)
                if (drawState.value) {
                    Draw.Canvas {
                        screenPoints = it
                    }
                } else {
                    screenPoints = emptyList()
                }

                if (draw == null) {
                    floating()
                } else {
                    DrawButton(drawState)
                }
            }
        }
    }

    @Composable
    private fun <T: URLs> BoxScope.Zoom(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        initialZoom: Float?,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        clearState: MutableState<Boolean>,
        touchState: MutableState<LatLng?>,
        screenPoints: List<Point>
    ) {
        val target = centerFromDraw(draw, center)
        val position = CameraPosition.fromLatLngZoom(target ?: LatLng(0.0, 0.0), if (target == null) 0f else initialZoom ?: 9f)
        MapType(appBar, title, gps, storage, layers, draw, poly, capture, clearState, touchState, screenPoints, position)
    }

    private fun centerFromDraw(draw: Draw.Model?, default: LatLng?): LatLng? {
        return if (draw != null) {
            val pts = draw.points
            if (pts.isNotEmpty()) {
                centerFromRing(pts)
            } else {
                default
            }
        } else {
            default
        }
    }

    @OptIn(FlowPreview::class)
    @Composable
    private fun <T : URLs> BoxScope.MapType(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        clearState: MutableState<Boolean>,
        touchState: MutableState<LatLng?>,
        screenPoints: List<Point>,
        position: CameraPosition
    ) {
        val (mapType, setMapType) = remember { mutableStateOf<MapType?>(null) }
        if (storage != null && mapType == null) {
            storage.getMapType(LocalContext.current.applicationContext, setMapType)
        } else {
            Checkers(appBar, title, gps, storage, layers, draw, poly, capture, clearState, touchState, screenPoints, position, mapType ?: MapType.HYBRID)
        }
    }

    @Composable
    private fun <T: URLs> BoxScope.Checkers(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        clearState: MutableState<Boolean>,
        touchState: MutableState<LatLng?>,
        screenPoints: List<Point>,
        position: CameraPosition,
        mapType: MapType,
    ) {
        val checkers = mutableListOf<Checker>()
        layers?.let {
            Layers.checkers(layers, checkers)
        }

        val (groups, setGroups) = remember { mutableStateOf<List<Polygons.NamedOptionsGroup>?>(null) }
        if (poly != null && groups == null) {
            Polygons.checkers(LocalContext.current.applicationContext, poly) { pair ->
                checkers.addAll(pair.first)
                setGroups(pair.second)
            }
        } else {
            Display(appBar, title, gps, storage, layers, draw, poly, capture, clearState, touchState, screenPoints, position, mapType, checkers, groups ?: emptyList())
        }
    }

    @Composable
    private fun <T : URLs> BoxScope.Display(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        clearState: MutableState<Boolean>,
        touchState: MutableState<LatLng?>,
        screenPoints: List<Point>,
        position: CameraPosition,
        mapType: MapType,
        checkers: List<Checker>,
        groups: List<Polygons.NamedOptionsGroup>,
    ) {
        val cameraPositionState = rememberCameraPositionState(init = { this.position = position })

        val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = gps, zoomControlsEnabled = false)) }
        var properties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = gps, mapType = mapType)) }

        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = properties, uiSettings = uiSettings, onMapClick = { pt ->
            touchState.value = pt
        }) {
            capture?.let {
                Capture.Display(capture, cameraPositionState)
            }

            layers?.let {
                Layers.Display(layers, checkers)
            }

            poly?.let {
                Polygons.Display(poly, groups, checkers, touchState)
            }

            if (checkers.isNotEmpty()) {
                appBar.Update(AppBarUpdate(
                    title = title,
                    actions = { DropdownMenu(checkers) }
                ))
            } else {
                appBar.Update(AppBarUpdate(title))
            }

            draw?.let {
                Draw.Do(draw, screenPoints, cameraPositionState, clearState)
            }
        }

        val context = LocalContext.current.applicationContext
        MapActionButton({
            val values = MapType.entries.toTypedArray()
            val size = values.size

            var next = (values.indexOf(properties.mapType) + 1) % size
            if (next == MapType.NONE.value) {
                next = (next + 1) % size
            }
            val mt = values[next]

            properties = MapProperties(isMyLocationEnabled = gps, mapType = mt)
            storage?.setMapType(context, mt)
        }, Alignment.TopStart) {
            Icon(Icons.Filled.Map, stringResource(R.string.next_base_map))
        }
    }

    @Composable
    private fun DropdownMenu(checkers: List<Checker>) {
        val (menu, setMenu) = remember { mutableStateOf(false) }
        IconButton(onClick = { setMenu(!menu) }) {
            Icon(Icons.Filled.Layers, stringResource(R.string.expand_layers_options_menu))
        }
        DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }) {
            checkers.forEach { checker ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    var checked by remember { mutableStateOf(checker.state) }
                    Checkbox(checked, onCheckedChange = {
                        checked = it
                        checker.setState(it)
                    })
                    Text(checker.name, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
    }
}