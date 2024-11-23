package org.blueventures.gemdroid.ui.common.maps

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds.centerFromRing
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import kotlin.math.roundToInt

object Compose {
    data class Checker(val name: String, var state: Boolean = true, var setState: (Boolean) -> Unit = {})

    @Composable
    fun <T : URLs> Screen(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        DrawingControls(appBar, title, gps, center, storage, layers, draw, poly, floating)
    }

    @Composable
    private fun <T : URLs> DrawingControls(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?,
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

                ZoomToMap(appBar, title, gps, center, storage, layers, poly, draw, clearState, touchState, screenPoints)
                if (drawState.value) {
                    MapDrawer {
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
    private fun <T: URLs> BoxScope.ZoomToMap(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        clearState: MutableState<Boolean>,
        touchState: MutableState<LatLng?>,
        screenPoints: List<Point>
    ) {
        val target = zoomOrDraw(center, draw)
        val position = CameraPosition.fromLatLngZoom(target ?: LatLng(0.0, 0.0), if (target == null) 0f else 9f)
        Map(appBar, title, gps, storage, layers, poly, draw, clearState, touchState, screenPoints, position)
    }

    @Composable
    private fun <T : URLs> BoxScope.Map(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        clearState: MutableState<Boolean>,
        touchState: MutableState<LatLng?>,
        screenPoints: List<Point>,
        position: CameraPosition
    ) {
        val ctx = LocalContext.current
        val (mapType, setMapType) = remember { mutableStateOf<MapType?>(null) }
        if (mapType == null && storage != null) {
            storage.getMapType(ctx, setMapType)
        } else {
            val cameraPositionState = rememberCameraPositionState(init = { this.position = position })
            val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = gps, zoomControlsEnabled = false)) }
            var properties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = gps, mapType = mapType!!)) }

            val checkers = tileCheckers(layers)
            val (pair, setPair) = remember { mutableStateOf<Pair<List<Checker>?, List<Poly.PolyOptionsGroup>?>?>(null) }
            when (pair) {
                null -> polygonCheckers(poly, setPair)
                else -> {
                    pair.first?.let {
                        checkers.addAll(it)
                    }

                    var groups: List<Poly.PolyOptionsGroup> = emptyList()
                    pair.second?.let {
                        groups = it
                    }

                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = properties, uiSettings = uiSettings, onMapClick = { pt ->
                        touchState.value = pt
                    }) {
                        layers?.let {
                            Tiles(layers, checkers)
                        }

                        poly?.let {
                            Polygons(poly, groups, checkers, touchState)
                        }

                        if (checkers.isNotEmpty()) {
                            appBar.Update(AppBarUpdate(
                                title = title,
                                actions = { LayersDropdown(checkers) }
                            ))
                        } else {
                            appBar.Update(AppBarUpdate(title))
                        }

                        draw?.let {
                            DoDrawing(draw, screenPoints, cameraPositionState, clearState)
                        }
                    }

                    MapActionButton({
                        val values = MapType.values()
                        val size = values.size

                        var next = (values.indexOf(properties.mapType) + 1) % size
                        if (next == MapType.NONE.value) {
                            next = (next + 1) % size
                        }
                        val mt = values[next]

                        properties = MapProperties(isMyLocationEnabled = gps, mapType = mt)
                        storage?.setMapType(ctx, mt)
                    }, Alignment.TopStart) {
                        Icon(Icons.Filled.Map, stringResource(R.string.next_base_map))
                    }
                }
            }
        }
    }

    private fun zoomOrDraw(center: LatLng?, draw: Draw.Model?): LatLng? {
        return if (draw != null) {
            val pts = draw.points
            if (pts.isNotEmpty()) {
                centerFromRing(pts)
            } else {
                center
            }
        } else {
            center
        }
    }

    @Composable
    private fun BoxScope.DrawButton(draw: MutableState<Boolean>) {
        MapActionButton({
            draw.value = !draw.value
        }) {
            if (draw.value) {
                Icon(Icons.Filled.Close, stringResource(R.string.stop_drawing_polygon))
            } else {
                Icon(Icons.Filled.Draw, stringResource(R.string.start_drawing_polygon))
            }
        }
    }

    enum class MotionEvent { IDLE, DOWN, UP, MOVE }
    data class MapPolygonState(
        val currentPosition: Offset = Offset.Unspecified,
        val event: MotionEvent = MotionEvent.IDLE
    )

    @Composable
    fun MapDrawer(onDrawingEnd : (List<Point>) -> Unit) {
        var state by remember { mutableStateOf(MapPolygonState()) }
        val brush = remember { SolidColor(Color.Green) }
        var path = remember { Path() }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val screenPoints = mutableListOf<Point>()
                        awaitPointerEvent().changes
                            .first()
                            .also { changes ->
                                val position = changes.position
                                screenPoints.add(position.toPoint())
                                state = state.copy(
                                    currentPosition = position,
                                    event = MotionEvent.DOWN
                                )
                            }
                        do {
                            val event: PointerEvent = awaitPointerEvent()
                            event.changes.forEach { changes ->
                                val position = changes.position
                                screenPoints.add(position.toPoint())
                                state = state.copy(
                                    currentPosition = position,
                                    event = MotionEvent.MOVE
                                )
                            }
                        } while (event.changes.any { it.pressed })

                        currentEvent.changes
                            .first()
                            .also { change ->
                                state = state.copy(
                                    currentPosition = change.position,
                                    event = MotionEvent.UP
                                )
                                screenPoints.add(change.position.toPoint())
                            }
                        val next = Path()
                        next.moveTo(state.currentPosition.x, state.currentPosition.y)
                        path = next
                        onDrawingEnd(screenPoints)
                    }
                },
            onDraw = {
                when (state.event) {
                    MotionEvent.IDLE -> Unit
                    MotionEvent.UP, MotionEvent.MOVE -> path.lineTo(
                        state.currentPosition.x,
                        state.currentPosition.y
                    )

                    MotionEvent.DOWN -> path.moveTo(
                        state.currentPosition.x,
                        state.currentPosition.y
                    )
                }
                drawPath(
                    path = path,
                    brush = brush,
                    style = Stroke(width = 8f)
                )
            }
        )
    }

    @Composable
    @GoogleMapComposable
    private fun DoDrawing(draw: Draw.Model, screenPoints: List<Point>, camera: CameraPositionState, clearState: MutableState<Boolean>) {
        if (screenPoints.isNotEmpty()) {
            camera.projection?.let {  proj ->
                draw.points = screenPoints.map {
                    proj.fromScreenLocation(it)
                }
            }
        }

        draw.polygonOptions()?.let { opt ->
            Polygon(points = opt.points, fillColor = Color(opt.fillColor), strokeColor = Color(opt.strokeColor), strokePattern = opt.strokePattern, strokeWidth = opt.strokeWidth, zIndex = 100f)
        }

        if (clearState.value) {
            draw.clear()
            clearState.value = false
        }
    }

    @Composable
    @GoogleMapComposable
    private fun <T : URLs> Tiles(tiles: Layers.Model<T>, checkers: List<Checker>) {
        val (urls, setUrls) = remember { mutableStateOf(tiles.initUrls) }
        if (staleCheck(urls)) {
            tiles.getRemote { result ->
                if (result is ApiResult.Success) {
                    val data = result.data!!
                    tiles.save(data)
                    setUrls(data)
                }
            }
        }

        tiles.layerNames.forEachIndexed { index, layer ->
            var chk: Checker? = null
            for (c in checkers) {
                if (c.name == layer) {
                    chk = c
                    break
                }
            }

            chk?.let { checker ->
                val opts = tiles.tileOpts(index, urls)
                opts.tileProvider?.let { provider ->
                    TileOverlay(provider, checker, opts)
                }
            }
        }
    }

    @Composable
    @GoogleMapComposable
    // this function prevents layers from flickering when toggled
    // by separating their contexts from each other
    private fun TileOverlay(provider: TileProvider, checker: Checker, opts: TileOverlayOptions) {
        val (visible, setVisible) = remember { mutableStateOf(true) }
        checker.state = visible
        checker.setState = setVisible
        TileOverlay(provider, visible = checker.state, zIndex = opts.zIndex)
    }

    @Composable
    private fun <T : URLs> tileCheckers(tiles: Layers.Model<T>?): MutableList<Checker> {
        val checkers = mutableListOf<Checker>()
        tiles?.layerNames?.forEach { name ->
            checkers.add(Checker(name))
        }
        return checkers
    }

    @Composable
    @GoogleMapComposable
    private fun Polygons(pmodel: Poly.Model, groups: List<Poly.PolyOptionsGroup>, checkers: List<Checker>, lastTouch: MutableState<LatLng?>) {
        val firstZ = 9999f

        val visibilities = mutableListOf<Boolean>()
        for (gindex in groups.indices) {
            val group = groups[gindex]
            val zIndex = firstZ - gindex

            var checker: Checker? = null
            for (c in checkers) {
                if (c.name == group.menuTitle) {
                    checker = c
                    break
                }
            }

            checker?.let {
                visibilities.add(it.state)
                MapPolygons(checker, zIndex, group)
            }
        }

        if (pmodel.touchEnabled) {
            PolygonTouch(pmodel, groups, visibilities, lastTouch)
        }
    }

    @Composable
    @GoogleMapComposable
    fun MapPolygons(checker: Checker, zIndex: Float, group: Poly.PolyOptionsGroup) {
        val (visible, setVisible) = remember { mutableStateOf(true) }
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

    @Composable
    private fun polygonCheckers(pmodel: Poly.Model?, callback: (Pair<MutableList<Checker>?, List<Poly.PolyOptionsGroup>?>) -> Unit) {
        if (pmodel == null) {
            callback(Pair(null, null))
            return
        }

        val ctx = LocalContext.current

        pmodel.polygonOptions(ctx) { groups ->
            val checkers = mutableListOf<Checker>()
            for (group in groups) {
                checkers.add(Checker(group.menuTitle))
            }
            callback(Pair(checkers, groups))
        }
    }

    @Composable
    @GoogleMapComposable
    private fun PolygonTouch(poly: Poly.Model, optGroups: List<Poly.PolyOptionsGroup>, visibility: List<Boolean>, lastTouch: MutableState<LatLng?>) {
        var markerOpts by remember { mutableStateOf<MarkerOptions?>(null) }
        markerOpts?.let {
            val state = MarkerState(it.position)
            MarkerInfoWindow(state = state, title = it.title)
        }

        lastTouch.value?.let { pt ->
            if (markerOpts?.position == pt) {
                lastTouch.value = null
            } else {
                poly.markerWork({
                    var mopts: MarkerOptions? = null
                    for (i in optGroups.indices) {
                        val optGroup = optGroups[i]

                        if (optGroup.namedOptions.isNotEmpty() && visibility[i]) {
                            for (namedOpts in optGroup.namedOptions) {
                                var contained = false
                                for (opts in namedOpts.options) {
                                    if (PolyUtil.containsLocation(pt, opts.points, true)) {
                                        mopts = MarkerOptions().position(pt).title(namedOpts.name)
                                        contained = true
                                        break
                                    }
                                }

                                if (contained) {
                                    break
                                }
                            }
                        }

                        if (mopts != null) {
                            break
                        }
                    }

                    mopts
                }) { opts ->
                    if (opts == null) {
                        lastTouch.value = null
                    }
                    markerOpts = opts
                }
            }
        }
    }

    @Composable
    private fun LayersDropdown(checkers: List<Checker>) {
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

private fun Offset.toPoint(): Point {
    return Point(x.roundToInt(), y.roundToInt())
}
