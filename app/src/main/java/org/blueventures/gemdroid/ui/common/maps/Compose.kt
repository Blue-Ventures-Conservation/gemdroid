package org.blueventures.gemdroid.ui.common.maps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds.centerFromList
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton

object Compose {
    data class Checker(override val name: String, val state: Boolean, val setState: (Boolean) -> Unit): Named

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
            val clearState = remember { mutableStateOf<Boolean?>(null) }
            draw?.let { draw ->
                Info.Row {
                    Butt.Text(stringResource(R.string.clear)) {
                        clearState.value = true
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
                val drawState = remember { mutableStateOf(false) }
                val removeState = remember { mutableStateOf<Boolean?>(null) }
                val touchState = remember { mutableStateOf<LatLng?>(null) }

                Zoom(appBar, title, gps, center, storage, layers, poly, draw, clearState, drawState, removeState, touchState)

                if (draw == null) {
                    floating()
                } else {
                    DrawButton(drawState, removeState, touchState)
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
        storage: Maps.Storage?,
        layers: Layers.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        clearState: MutableState<Boolean?>,
        drawState: MutableState<Boolean>,
        removeState: MutableState<Boolean?>,
        touchState: MutableState<LatLng?>
    ) {
        val target = zoomOrDraw(center, draw)
        val position = CameraPosition.fromLatLngZoom(target ?: LatLng(0.0, 0.0), if (target == null) 0f else 9f)
        Map(appBar, title, gps, storage, layers, poly, draw, clearState, drawState, removeState, touchState, position)
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
        clearState: MutableState<Boolean?>,
        drawState: MutableState<Boolean>,
        removeState: MutableState<Boolean?>,
        touchState: MutableState<LatLng?>,
        position: CameraPosition
    ) {
        val ctx = LocalContext.current
        val (mapTypeResult, setMapTypeResult) = remember { mutableStateOf<Result<Int>?>(null) }
        if (mapTypeResult == null && storage != null) {
            storage.getMapType(ctx, setMapTypeResult)
        } else {
            val mapType = if (mapTypeResult?.isSuccess == true) getMapType(mapTypeResult.getOrNull()!!) else MapType.HYBRID
            val cameraPositionState = rememberCameraPositionState(init = { this.position = position })
            val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = gps, zoomControlsEnabled = false)) }
            var properties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = gps, mapType = mapType)) }
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = properties, uiSettings = uiSettings, onMapClick = { pt ->
                touchState.value = pt
            }) {
                val checkers = mutableListOf<Checker>()
                layers?.let {
                    Tiles(layers, checkers)
                }

                poly?.let {
                    Polygons(poly, checkers, touchState)
                }

                if (checkers.isNotEmpty()) {
                    appBar.Update(AppBarUpdate(
                        title = title,
                        actions = { LayersDropdown(checkers) }
                    ))
                }

                draw?.let {
                    DrawTouch(draw, clearState, drawState, removeState, touchState)
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
                storage?.setMapType(ctx, mt.value)
            }, Alignment.TopStart) {
                Icon(Icons.Filled.Map, stringResource(R.string.next_base_map))
            }
        }
    }

    private fun getMapType(from: Int): MapType {
        for (t in MapType.values()) {
            if (t.value == from) {
                return t
            }
        }

        return MapType.HYBRID
    }

    private fun zoomOrDraw(center: LatLng?, draw: Draw.Model?): LatLng? {
        return if (draw != null) {
            val pts = draw.points()
            if (pts.isNotEmpty()) {
                centerFromList(pts)
            } else {
                center
            }
        } else {
            center
        }
    }

    @Composable
    private fun BoxScope.DrawButton(draw: MutableState<Boolean>, remove: MutableState<Boolean?>, touchState: MutableState<LatLng?>) {
        MapActionButton({ remove.value = true }, Alignment.BottomStart) {
            Icon(Icons.AutoMirrored.Filled.Backspace, stringResource(R.string.delete_the_previous_point))
        }
        MapActionButton({
            touchState.value = null
            draw.value = !draw.value
        }) {
            if (draw.value) {
                Icon(Icons.Filled.Close, stringResource(R.string.stop_drawing_polygon))
            } else {
                Icon(Icons.Filled.Place, stringResource(R.string.place_polygon_corner))
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun DrawTouch(draw: Draw.Model, clearState: MutableState<Boolean?>, drawState: MutableState<Boolean>, removeState: MutableState<Boolean?>, lastTouch: MutableState<LatLng?>) {
        val points = draw.points()
        var pointCount by remember { mutableIntStateOf(points.size) }
        if (pointCount > 0) {
            for (point in points) {
                Marker(state = MarkerState(point))
            }
        }

        val (polyOpts, setPolyOpts) = remember { mutableStateOf(draw.polygonOptions()) }
        polyOpts?.let { opt ->
            Polygon(points = opt.points, fillColor = Color(opt.fillColor), zIndex = 100f)
        }

        val resetLocalState = {
            pointCount = points.size
            setPolyOpts(draw.polygonOptions())
        }

        if (clearState.value != null) {
            draw.clear()
            clearState.value = null
            lastTouch.value = null
            resetLocalState()
        } else {
            if (removeState.value != null) {
                draw.removePrev { didRemove ->
                    if (didRemove != null) {
                        resetLocalState()
                        lastTouch.value = null
                    }

                    removeState.value = null
                }
            } else {
                if (drawState.value) {
                    lastTouch.value?.let { pt ->
                        if (points.isEmpty() || points.last() != pt) {
                            val ctx = LocalContext.current
                            draw.addPoint(pt) { err ->
                                if (err != null) {
                                    draw.snack(ctx.getString(err).format(draw.maxPoints.toString()))
                                } else {
                                    resetLocalState()
                                }
                                lastTouch.value = null
                            }
                        }
                    }
                } else {
                    lastTouch.value = null
                }
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun <T : URLs> Tiles(tiles: Layers.Model<T>, checkers: MutableList<Checker>) {
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
            val opts = tiles.tileOpts(index, urls)
            opts.tileProvider?.let { provider ->
                TileOverlay(layer, provider, opts.zIndex, checkers)
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun TileOverlay(name: String, provider: TileProvider, zIndex: Float, checkers: MutableList<Checker>) {
        val (checked, setChecked) = remember { mutableStateOf(true) }
        updateList(Checker(name, checked, setChecked), checkers)
        TileOverlay(tileProvider = provider, visible = checked, zIndex = zIndex)
    }

    @Composable
    @GoogleMapComposable
    private fun Polygons(poly: Poly.Model, checkers: MutableList<Checker>, lastTouch: MutableState<LatLng?>) {
        val (polyOptsGroups, setPolyOptsGroups) = remember { mutableStateOf<List<Poly.PolyOptionsGroup>?>(null) }
        when (polyOptsGroups) {
            null -> {
                poly.polygonOptions(setPolyOptsGroups)
            }
            else -> {
                var totalPolys = 0
                val prevCounts = mutableListOf(0)
                for (polyOpts in polyOptsGroups) {
                    totalPolys += polyOpts.options.size
                    prevCounts.add(totalPolys)
                }

                val firstZ = 99f

                val visibilityState = mutableListOf<Boolean>()
                for (gindex in polyOptsGroups.indices) {
                    val startingZ = firstZ - prevCounts[gindex]
                    val polyOpts = polyOptsGroups[gindex]

                    if (polyOpts.options.isNotEmpty()) {
                        val (checked, setChecked) = remember { mutableStateOf(polyOpts.startChecked) }
                        updateList(Checker(stringResource(polyOpts.menuTitle), checked, setChecked), checkers)
                        visibilityState.add(checked)
                        for (i in polyOpts.options.indices) {
                            val opt = polyOpts.options[i]
                            val zIndex = startingZ + i
                            Polygon(points = opt.options.points, fillColor = Color(opt.options.fillColor), visible = checked, zIndex = zIndex)
                        }
                    }
                }

                if (poly.touchEnabled) {
                    PolygonTouch(poly, polyOptsGroups, visibilityState, lastTouch)
                }
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun PolygonTouch(poly: Poly.Model, optGroups: List<Poly.PolyOptionsGroup>, visibility: List<Boolean>, lastTouch: MutableState<LatLng?>) {
        var markerOpts by remember { mutableStateOf<MarkerOptions?>(null) }
        markerOpts?.let {
            val state = MarkerState(it.position)
            state.showInfoWindow()
            Marker(state = state, title = it.title)
        }

        lastTouch.value?.let { pt ->
            if (markerOpts?.position == pt) {
                lastTouch.value = null
            } else {
                poly.markerWork({
                    var mopts: MarkerOptions? = null
                    for (i in optGroups.indices) {
                        val opts = optGroups[i]

                        if (opts.options.isNotEmpty() && visibility[i]) {
                            for (j in opts.options.indices) {
                                val opt = opts.options[j]
                                if (PolyUtil.containsLocation(pt, opt.options.points, true)) {
                                    mopts = MarkerOptions().position(pt).title(opt.name)
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

    interface Named {
        val name: String
    }

    private fun <T : Named> updateList(named: T, nameds: MutableList<T>) {
        var removeIndex = -1
        var i = 0
        nameds.removeIf {
            val ret = named.name == it.name
            if (ret) {
                removeIndex = i
            }
            i++
            ret
        }

        if (removeIndex > -1) {
            nameds.add(removeIndex, named)
        } else {
            nameds.add(named)
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