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
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.TileProvider
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
    data class Checker(val name: String, val state: Boolean, val setState: (Boolean) -> Unit)

    @Composable
    fun <T : URLs> Screen(
        gps: Boolean,
        tiles: Tiles.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        DrawingControls(gps, tiles, draw, poly, floating)
    }

    @Composable
    private fun <T : URLs> DrawingControls(
        gps: Boolean,
        tiles: Tiles.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (clear, setClear) = remember { mutableStateOf<Boolean?>(null) }
            draw?.let { draw ->
                Info.Row {
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
                val db = drawButton()
                Zoom(gps, tiles, poly, draw, db.getState, setClear) { clear }

                if (draw == null) {
                    floating()
                } else {
                    db.content()
                }
            }
        }
    }

    @Composable
    private fun <T: URLs> Zoom(
        gps: Boolean,
        tiles: Tiles.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        drawing: () -> Boolean,
        setClear: (Boolean?) -> Unit,
        getClear: () -> Boolean?,
    ) {
        val (zoomed, setZoomed) = remember { mutableStateOf(false) }
        var center = shouldZoom(zoomed, tiles, draw)
        if (!zoomed && center != null) {
            setZoomed(true)
        } else if (zoomed) {
            center = null
        }

        val position = CameraPosition.fromLatLngZoom(center ?: LatLng(0.0, 0.0), if (center == null) 0f else 9f)
        Map(gps, tiles, poly, draw, drawing, setClear, getClear, position)
    }

    @Composable
    private fun <T : URLs> Map(
        gps: Boolean,
        tiles: Tiles.Model<T>?,
        poly: Poly.Model?,
        draw: Draw.Model?,
        drawing: () -> Boolean,
        setClear: (Boolean?) -> Unit,
        getClear: () -> Boolean?,
        position: CameraPosition
    ) {
        val cameraPositionState = rememberCameraPositionState(init = { this.position = position })
        val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = gps, zoomControlsEnabled = false)) }
        val properties by remember { mutableStateOf(MapProperties(mapType = MapType.SATELLITE)) }
        val (touch, setTouch) = remember { mutableStateOf<LatLng?>(null) }

        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = properties, uiSettings = uiSettings, onMapClick = { point ->
            setTouch(point)
        }) {
            val checkers = mutableListOf<Checker>()
            tiles?.let {
                Tiles(tiles, checkers)
            }

            poly?.let {
                Polygons(poly, checkers)
            }

            tiles?.let {
                tiles.appBar.Update(AppBarUpdate(
                    title = tiles.title,
                    actions = { LayersDropdown(checkers) }
                ))
            }

            draw?.let {
                HandleTouch(draw, drawing, setClear, getClear, setTouch) { touch }
            }
        }
    }

    private fun <T : URLs> shouldZoom(zoomed: Boolean, tiles: Tiles.Model<T>?, draw: Draw.Model?): LatLng? {
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
    private fun BoxScope.drawButton(): DrawButton  {
        val (drawing, setDrawing) = remember { mutableStateOf(false) }
        return DrawButton({ drawing }) {
            MapActionButton({ setDrawing(!drawing) }) {
                if (drawing) {
                    Icon(Icons.Filled.Close, stringResource(R.string.stop_drawing_polygon))
                } else {
                    Icon(Icons.Filled.Place, stringResource(R.string.place_polygon_corner))
                }
            }
        }
    }

    @Composable
    @GoogleMapComposable
    private fun HandleTouch(draw: Draw.Model, drawing: () -> Boolean, setClear: (Boolean?) -> Unit, getClear: () -> Boolean?, setTouch: (LatLng?) -> Unit, touch: () -> LatLng?) {
        var pointCount by remember { mutableIntStateOf(draw.points.size) }
        if (pointCount > 0) {
            for (point in draw.points) {
                Marker(state = MarkerState(point))
            }
        }

        val (polyOpts, setPolyOpts) = remember { mutableStateOf(draw.polygonOptions()) }
        polyOpts?.let { opt ->
            Polygon(points = opt.points, fillColor = Color(opt.fillColor))
        }

        getClear()?.let {
            draw.points.clear()
            pointCount = draw.points.size
            setPolyOpts(draw.polygonOptions())
            setClear(null)
            setTouch(null)
        } ?: run {
            if (drawing()) {
                touch()?.let { pt ->
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
    private fun <T : URLs> Tiles(tiles: Tiles.Model<T>, checkers: MutableList<Checker>) {
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
        updateCheckers(Checker(name, checked, setChecked), checkers)
        TileOverlay(tileProvider = provider, visible = checked, zIndex = zIndex)
    }

    @Composable
    @GoogleMapComposable
    private fun Polygons(poly: Poly.Model, checkers: MutableList<Checker>) {
        val (polyOpts, setPolyOpts) = remember { mutableStateOf<List<PolygonOptions>?>(null) }
        when (polyOpts) {
            null -> {
                poly.polygonOptions(setPolyOpts)
            }
            else -> {
                val (checked, setChecked) = remember { mutableStateOf(true) }
                updateCheckers(Checker(poly.menuTitle, checked, setChecked), checkers)
                for (opt in polyOpts) {
                    Polygon(points = opt.points, fillColor = Color(opt.fillColor), visible = checked)
                }
            }
        }
    }

    private fun updateCheckers(checker: Checker, checkers: MutableList<Checker>) {
        var removeIndex = -1
        var i = 0
        checkers.removeIf {
            val ret = checker.name == it.name
            if (ret) {
                removeIndex = i
            }
            i++
            ret
        }

        if (removeIndex > -1) {
            checkers.add(removeIndex, checker)
        } else {
            checkers.add(checker)
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