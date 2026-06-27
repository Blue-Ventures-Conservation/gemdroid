package org.blueventures.gemdroid.ui.common.maps

import android.Manifest
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission
import org.blueventures.gemdroid.ui.common.maps.Capture.CaptureMapActions
import org.blueventures.gemdroid.ui.common.maps.Draw.DrawMapActions

data class ClickContent(val click: Click, val content: @Composable () -> Unit)
object Maps {
    interface Storage {
        fun getMapType(context: Context, callback: (MapType) -> Unit): Job
        fun setMapType(context: Context, mapType: MapType): Job

        companion object {
            val mapTypeKey = intPreferencesKey("map_type_key")

            fun fromViewModel(viewModel: ApiViewModel) = object : Storage {
                override fun getMapType(context: Context, callback: (MapType) -> Unit) = viewModel.read(context, mapTypeKey, MapType.HYBRID.value) { callback(convert(it)) }
                override fun setMapType(context: Context, mapType: MapType) = viewModel.write(context, mapTypeKey, mapType.value)
            }

            private fun convert(from: Int): MapType {
                for (t in MapType.entries) {
                    if (t.value == from) {
                        return t
                    }
                }

                return MapType.HYBRID
            }
        }
    }

    data class Checker(val name: String, var state: Boolean = true, var setState: (Boolean) -> Unit = {})
    data class FloatingNext(val imageVector: ImageVector, @StringRes val contentDescription: Int, val next: Click)

    @Composable
    fun NoLayers(
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        center: LatLng? = null,
        initialZoom: Float? = null,
        storage: Storage? = null,
        draw: Draw.Model? = null,
        poly: Polygons.Model? = null,
        capture: Capture.Model? = null,
        next: FloatingNext? = null,
    ) {
        Screen<URLs>(appBar, title, attemptGps, center, initialZoom, storage, null, draw, poly, capture, next)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T : URLs> Screen(
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        center: LatLng? = null,
        initialZoom: Float? = null,
        storage: Storage? = null,
        layers: Layers.Model<T>? = null,
        draw: Draw.Model? = null,
        poly: Polygons.Model? = null,
        capture: Capture.Model? = null,
        next: FloatingNext? = null,
    ) {
        if (attemptGps) {
            RequestPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                rationale = stringResource(R.string.gps_rationale),
                description = stringResource(R.string.gps_rationale_description),
                optional = true
            ) { granted ->
                MapType(appBar, title, granted, center, initialZoom, storage, layers, draw, poly, capture, next)
            }
        } else {
            MapType(appBar, title, false, center, initialZoom, storage, layers, draw, poly, capture, next)
        }
    }

    @Composable
    private fun <T : URLs> MapType(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        initialZoom: Float?,
        storage: Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        next: FloatingNext?,
    ) {
        val (mapType, setMapType) = remember { mutableStateOf<MapType?>(null) }
        if (storage != null && mapType == null) {
            storage.getMapType(LocalContext.current.applicationContext, setMapType)
        } else {
            Checkers(appBar, title, gps, center, initialZoom, storage, layers, draw, poly, capture, mapType ?: MapType.HYBRID, next)
        }
    }

    @Composable
    private fun <T: URLs> Checkers(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        initialZoom: Float?,
        storage: Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        mapType: MapType,
        next: FloatingNext?,
    ) {
        val checkers = remember { mutableListOf<Checker>() }
        if (layers != null && checkers.isEmpty()) {
            Layers.checkers(layers, checkers)
        }

        val (groups, setGroups) = remember { mutableStateOf<List<Polygons.NamedOptionsGroup>?>(null) }
        if (poly != null && groups == null) {
            Polygons.checkers(LocalContext.current.applicationContext, poly) { pair ->
                checkers.addAll(pair.first)
                setGroups(pair.second)
            }
        } else {
            Display(appBar, title, gps, center, initialZoom, storage, layers, draw, poly, capture, mapType, checkers, groups ?: emptyList(), next)
        }
    }

    @Composable
    private fun <T : URLs> Display(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        center: LatLng?,
        initialZoom: Float?,
        storage: Storage?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        mapType: MapType,
        checkers: List<Checker>,
        groups: List<Polygons.NamedOptionsGroup>,
        next: FloatingNext?,
    ) {
        val cameraPosition = CameraPosition.fromLatLngZoom(center ?: LatLng(0.0, 0.0), if (center == null) 0f else initialZoom ?: 9f)
        val cameraPositionState = rememberCameraPositionState(init = { position = cameraPosition })

        val uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = true, myLocationButtonEnabled = gps, zoomControlsEnabled = false)) }
        val (properties, setProperties) = remember { mutableStateOf(MapProperties(isMyLocationEnabled = gps, mapType = mapType)) }
        val touchState = remember { mutableStateOf<LatLng?>(null) }
        val drawState = Draw.prepareState()
        var captureState: MutableState<Capture.CaptureState>? = null
        if (capture != null) {
            captureState = Capture.prepareState(capture, cameraPositionState)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, start = 12.dp, end = 12.dp),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings,
                onMapClick = { pt ->
                    touchState.value = pt
                }) {
                // drawing and capturing are currently mutually exclusive
                // because they both take over the bottom right floating map buttons
                draw?.let {
                    Draw.Display(draw, drawState, cameraPositionState)
                } ?: capture?.let {
                    Capture.Display(capture, captureState!!)
                }

                layers?.let {
                    Layers.Display(layers, checkers)
                }

                poly?.let {
                    Polygons.Display(poly, groups, checkers, touchState)
                }

                if (checkers.isNotEmpty()) {
                    appBar.Update(
                        AppBarUpdate(
                            title = title,
                            actions = { DropdownMenu(checkers) }
                        ))
                } else {
                    appBar.Update(AppBarUpdate(title))
                }
            }

            MapTypeButton(storage, properties, setProperties)

            draw?.let {
                DrawMapActions(draw, drawState)
            } ?: capture?.let {
                CaptureMapActions(capture, captureState!!)
            }

            if (capture == null && draw == null && next != null) {
                OptionalNextButton(next)
            }
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

    @Composable
    fun BoxScope.MapTypeButton(storage: Storage?, properties: MapProperties, setProperties: (MapProperties) -> Unit) {
        val context = LocalContext.current.applicationContext
        MapActionButton({
            val values = MapType.entries.toTypedArray()
            val size = values.size

            var next = (values.indexOf(properties.mapType) + 1) % size
            if (next == MapType.NONE.value) {
                next = (next + 1) % size
            }
            val mt = values[next]

            setProperties(MapProperties(isMyLocationEnabled = properties.isMyLocationEnabled, mapType = mt))
            storage?.setMapType(context, mt)
        }, Alignment.BottomStart) {
            Icon(Icons.Filled.Map, stringResource(R.string.next_base_map))
        }
    }

    @Composable
    fun BoxScope.OptionalNextButton(next: FloatingNext) {
        MapActionButton(next.next) { Icon(next.imageVector, stringResource(next.contentDescription)) }
    }

    @Composable
    fun BoxScope.MapActionButton(click: Click, align: Alignment = Alignment.BottomEnd, content: @Composable () -> Unit) {
        FloatingActionButton(click, modifier = Modifier
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp)
            .align(align),
            content = content,
        )
    }

    @Composable
    fun BoxScope.MultiMapActionButtons(vararg clickContents: ClickContent) {
        Column(Modifier
            .padding(16.dp, 12.dp, 16.dp, 24.dp)
            .align(Alignment.BottomEnd),
            Arrangement.spacedBy(16.dp, Alignment.Bottom)) {
            for (clickContent in clickContents) {
                FloatingActionButton(clickContent.click, content = clickContent.content)
            }
        }
    }
}