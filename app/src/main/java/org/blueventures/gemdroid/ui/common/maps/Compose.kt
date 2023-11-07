package org.blueventures.gemdroid.ui.common.maps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.zibnix.droidbones.api.ApiResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
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
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.data.staleCheck
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton

object Compose {
    data class Checker(override val name: String, val state: Boolean, val setState: (Boolean) -> Unit): Named
    data class Toucher(override val name: String, val state: LatLng?, val setState: (LatLng?) -> Unit): Named
    data class Clearer(override val name: String, val state: Boolean?, val setState: (Boolean?) -> Unit): Named
    data class Drawer(override val name: String, val state: Boolean, val setState: (Boolean) -> Unit): Named

    @Composable
    fun <T : URLs> Screen(
        gps: Boolean,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        DrawingControls(gps, layers, draw, poly, floating)
    }

    @Composable
    private fun <T : URLs> DrawingControls(
        gps: Boolean,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val clearers = remember { mutableStateListOf<Clearer>() }
            draw?.let { draw ->
                Info.Row {
                    val (clear, setClear) = remember { mutableStateOf<Boolean?>(null) }
                    updateList(Clearer("clear", clear, setClear), clearers)
                    Butt.Text(stringResource(R.string.clear)) {
                        setClear(true)
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
                                val max = draw.maxSquareKms()
                                val current = draw.polygonSquareKms()
                                tooBig.format(max, current)
                            }

                            draw.snack(msg)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val drawers = remember { mutableStateListOf<Drawer>() }
                Zoom(gps, layers, poly, draw, drawers, clearers)

                if (draw == null) {
                    floating()
                } else {
                    DrawButton(drawers)
                }
            }
        }
    }

    @Composable
    private fun <T: URLs> Zoom(
        gps: Boolean,
        layers: Layers.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        drawers: List<Drawer>,
        clearers: List<Clearer>
    ) {
        val (zoomed, setZoomed) = remember { mutableStateOf(false) }
        var center = shouldZoom(zoomed, layers, draw)
        if (!zoomed && center != null) {
            setZoomed(true)
        } else if (zoomed) {
            center = null
        }

        val position = CameraPosition.fromLatLngZoom(center ?: LatLng(0.0, 0.0), if (center == null) 0f else 9f)
        Map(gps, layers, poly, draw, drawers, clearers, position)
    }

    @Composable
    private fun <T : URLs> Map(
        gps: Boolean,
        layers: Layers.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        drawers: List<Drawer>,
        clearers: List<Clearer>,
        position: CameraPosition
    ) {
        val cameraPositionState = rememberCameraPositionState(init = { this.position = position })
        val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = gps, zoomControlsEnabled = false)) }
        val properties by remember { mutableStateOf(MapProperties(mapType = MapType.SATELLITE)) }
        val touchers = remember { mutableStateListOf<Toucher>() }
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = properties, uiSettings = uiSettings, onMapClick = { pt ->
            for (toucher in touchers) {
                toucher.setState(pt)
            }
        }) {
            val checkers = mutableListOf<Checker>()
            layers?.let {
                Tiles(layers, checkers)
            }

            poly?.let {
                Polygons(poly, checkers, touchers)
            }

            layers?.let {
                layers.appBar.Update(AppBarUpdate(
                    title = layers.title,
                    actions = { LayersDropdown(checkers) }
                ))
            }

            draw?.let {
                DrawTouch(draw, drawers, clearers, touchers)
            }
        }
    }

    private fun <T : URLs> shouldZoom(zoomed: Boolean, tiles: Layers.Model<T>?, draw: Draw.Model?): LatLng? {
        return if (draw == null) {
            tiles?.bounds
        } else {
            if (!zoomed) {
                draw.points.ifEmpty {
                    tiles?.bounds
                }
            } else {
                null
            }
        }?.let { bounds ->
            centerFromList(bounds)
        }
    }

    private fun centerFromList(list: List<LatLng>): LatLng {
        val builder = LatLngBounds.builder()
        for (pt in list) {
            builder.include(pt)
        }
        return builder.build().center
    }

    data class DrawButton(val getState: () -> Boolean, val content: @Composable () -> Unit)

    @Composable
    private fun BoxScope.DrawButton(drawers: MutableList<Drawer>) {
        val (buttonState, setButtonState) = remember { mutableStateOf(false) }
        updateList(Drawer("draw", buttonState, setButtonState), drawers)
        MapActionButton({ setButtonState(!buttonState) }) {
            if (buttonState) {
                Icon(Icons.Filled.Close, stringResource(R.string.stop_drawing_polygon))
            } else {
                Icon(Icons.Filled.Place, stringResource(R.string.place_polygon_corner))
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun DrawTouch(draw: Draw.Model, drawers: List<Drawer>, clearers: List<Clearer>, touchers: MutableList<Toucher>) {
        var pointCount by remember { mutableIntStateOf(draw.points.size) }
        if (pointCount > 0) {
            for (point in draw.points) {
                Marker(state = MarkerState(point))
            }
        }

        val (polyOpts, setPolyOpts) = remember { mutableStateOf(draw.polygonOptions()) }
        polyOpts?.let { opt ->
            Polygon(points = opt.points, fillColor = Color(opt.fillColor), zIndex = 100f)
        }

        val (touch, setTouch) = remember { mutableStateOf<LatLng?>(null) }
        updateList(Toucher("drawing", touch, setTouch), touchers)
        val clearer = clearers.first()
        clearer.state?.let {
            draw.points.clear()
            pointCount = draw.points.size
            setPolyOpts(draw.polygonOptions())
            clearer.setState(null)
            setTouch(null)
        } ?: run {
            val drawer = drawers.first()
            if (drawer.state) {
                touch?.let { pt ->
                    if (draw.points.isEmpty() || draw.points.last() != pt) {
                        draw.addPoint(pt) {
                            pointCount = draw.points.size
                            setPolyOpts(draw.polygonOptions())
                        }
                    }
                }
            } else {
                setTouch(null)
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
    private fun Polygons(poly: Poly.Model, checkers: MutableList<Checker>, touchers: MutableList<Toucher>) {
        val (polyOpts, setPolyOpts) = remember { mutableStateOf<List<PolygonOptions>?>(null) }
        when (polyOpts) {
            null -> {
                poly.polygonOptions(setPolyOpts)
            }
            else -> {
                val (checked, setChecked) = remember { mutableStateOf(true) }
                updateList(Checker(poly.menuTitle, checked, setChecked), checkers)
                for (opt in polyOpts) {
                    Polygon(points = opt.points, fillColor = Color(opt.fillColor), visible = checked, zIndex = 99f)
                }

                if (poly.touchEnabled) {
                    PolygonTouch(poly, polyOpts, touchers)
                }
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun PolygonTouch(poly: Poly.Model, opts: List<PolygonOptions>, touchers: MutableList<Toucher>) {
        var markerOpts by remember { mutableStateOf<MarkerOptions?>(null) }
        markerOpts?.let {
            val state = MarkerState(it.position)
            state.showInfoWindow()
            Marker(state = state, title = it.title)
        }

        val (touch, setTouch) = remember { mutableStateOf<LatLng?>(null) }
        updateList(Toucher("polygons", touch, setTouch), touchers)
        touch?.let { pt ->
            if (markerOpts?.position == pt) {
                setTouch(null)
            } else {
                poly.markerWork({
                    var mopts: MarkerOptions? = null
                    for (i in opts.indices) {
                        val opt = opts[i]
                        val label = poly.labels[i]
                        if (PolyUtil.containsLocation(pt, opt.points, true)) {
                            mopts = MarkerOptions().position(pt).title(label)
                            break
                        }
                    }

                    mopts
                }) { opts ->
                    if (opts == null) {
                        setTouch(null)
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
            Icon(Icons.Filled.MoreVert, "")
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