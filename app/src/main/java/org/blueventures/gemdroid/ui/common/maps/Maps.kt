package org.blueventures.gemdroid.ui.common.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.ClickContent
import org.blueventures.gemdroid.ui.common.FloatingButton
import org.blueventures.gemdroid.ui.common.FloatingButtons
import org.blueventures.gemdroid.ui.common.RequestPermission
import org.blueventures.gemdroid.ui.common.maps.Capture.CaptureMapActions
import org.blueventures.gemdroid.ui.common.maps.Draw.DrawMapActions

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

    data class Checker(var name: String = "", var state: Boolean = true, var setState: (Boolean) -> Unit = {})
    data class FloatingNext(val imageVector: ImageVector, val contentDescription: Int, val next: Click)

    @Composable
    fun NoLayers(
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        storage: Storage? = null,
        bounds: LatLngBounds? = null,
        draw: Draw.Model? = null,
        poly: Polygons.Model? = null,
        capture: Capture.Model? = null,
        next: FloatingNext? = null,
    ) {
        Screen<URLs>(appBar, title, attemptGps, storage, bounds, null, draw, poly, capture, next)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T : URLs> Screen(
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        storage: Storage? = null,
        bounds: LatLngBounds? = null,
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
                MapType(appBar, title, granted, storage, bounds, layers, draw, poly, capture, next)
            }
        } else {
            MapType(appBar, title, false, storage, bounds, layers, draw, poly, capture, next)
        }
    }

    @Composable
    private fun <T : URLs> MapType(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Storage?,
        bounds: LatLngBounds?,
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
            Checkers(appBar, title, gps, storage, bounds, layers, draw, poly, capture, mapType ?: MapType.HYBRID, next)
        }
    }

    @Composable
    private fun <T: URLs> Checkers(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Storage?,
        bounds: LatLngBounds?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        mapType: MapType,
        next: FloatingNext?,
    ) {
        val checkers = remember { mutableListOf<Checker>() }
        if (checkers.isEmpty() && layers != null) {
            Layers.checkers(layers, checkers)
        }

        val polygonGroups = remember { mutableListOf<Polygons.NamedOptionsGroup>() }
        val (polyStateGathered, setPolyStateGathered) = remember { mutableStateOf(false) }
        if (poly != null && !polyStateGathered) {
            Polygons.checkers(LocalContext.current, poly) { pair ->
                checkers.addAll(pair.first)
                polygonGroups.addAll(pair.second)
                setPolyStateGathered(true)
            }
        } else {
            Display(appBar, title, gps, storage, bounds, layers, draw, poly, capture, mapType, checkers, polygonGroups, next)
        }
    }

    @Composable
    private fun <T : URLs> Display(
        appBar: AppBar,
        title: String,
        gps: Boolean,
        storage: Storage?,
        bounds: LatLngBounds?,
        layers: Layers.Model<T>?,
        draw: Draw.Model?,
        poly: Polygons.Model?,
        capture: Capture.Model?,
        mapType: MapType,
        checkers: MutableList<Checker>,
        polygonGroups: List<Polygons.NamedOptionsGroup>,
        next: FloatingNext?,
    ) {
        val zero = LatLng(0.0, 0.0)
        val center = bounds?.center ?: zero
        MapsInitializer.initialize(LocalContext.current.applicationContext)
        val update = if (bounds != null) CameraUpdateFactory.newLatLngBounds(bounds, 32) else CameraUpdateFactory.newLatLngZoom(zero, 0f)
        val cameraPositionState = rememberCameraPositionState(init = { move(update) })
        val uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = true, myLocationButtonEnabled = false, zoomControlsEnabled = false)) }
        val (properties, setProperties) = remember { mutableStateOf(MapProperties(isMyLocationEnabled = gps, mapType = mapType)) }
        val touchState = remember { mutableStateOf<LatLng?>(null) }
        val drawState = Draw.prepareState()
        var captureState: MutableState<Capture.State>? = null
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
                layers?.let {
                    Layers.Display(layers, checkers)
                }

                poly?.let {
                    Polygons.Display(poly.touchEnabled, poly::onTouch, polygonGroups, checkers, touchState)
                }

                // drawing and capturing are currently mutually exclusive
                // because they both take over the bottom right floating map buttons
                draw?.let {
                    Draw.Display(draw, drawState, cameraPositionState)
                } ?: capture?.let {
                    Capture.Display(capture,  captureState!!, checkers, touchState)
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

            MapButtons(storage, update, center, cameraPositionState, properties, setProperties)

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
    fun BoxScope.MapButtons(storage: Storage?, initialUpdate: CameraUpdate, center: LatLng, cameraPositionState: CameraPositionState, properties: MapProperties, setProperties: (MapProperties) -> Unit) {
        val context = LocalContext.current.applicationContext
        val contents = mutableListOf(ClickContent({
            val values = MapType.entries.toTypedArray()
            val size = values.size

            var next = (values.indexOf(properties.mapType) + 1) % size
            if (next == MapType.NONE.value) {
                next = (next + 1) % size
            }
            val mt = values[next]

            setProperties(MapProperties(isMyLocationEnabled = properties.isMyLocationEnabled, mapType = mt))
            storage?.setMapType(context, mt)
        }, {
            Icon(Icons.Filled.Map, stringResource(R.string.next_base_map))
        }))

        val scope = rememberCoroutineScope()
        contents.add(0, ClickContent({
            zoomToLocation(scope, cameraPositionState, initialUpdate, center)
        }, {
            Icon(Icons.Filled.LocationOn, stringResource(R.string.zoom_to_your_area_of_interest))
        }))

        if (properties.isMyLocationEnabled) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                contents.add(0, ClickContent({
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            zoomToLocation(scope, cameraPositionState, CameraUpdateFactory.newLatLngZoom(userLatLng, 15f), userLatLng)
                        }
                    }
                }, {
                    Icon(Icons.Filled.MyLocation, stringResource(R.string.zoom_to_your_gps_location))
                }))
            }
        }

        FloatingButtons(Alignment.BottomStart, contents)
    }

    private fun zoomToLocation(scope: CoroutineScope, cameraPositionState: CameraPositionState, update: CameraUpdate, center: LatLng) {
        val isContained = cameraPositionState.projection?.visibleRegion?.latLngBounds?.contains(center) == true
        val minZoom = 5f
        scope.launch {
            try {
                if (!isContained) {
                    cameraPositionState.animate(CameraUpdateFactory.zoomTo(minZoom), 800)
                }

                cameraPositionState.animate(update, 1250)
            } catch (_: Exception) {
                // pass
            }
        }
    }

    @Composable
    fun BoxScope.OptionalNextButton(next: FloatingNext) {
        FloatingButton(next.next) { Icon(next.imageVector, stringResource(next.contentDescription)) }
    }
}