package org.blueventures.gemdroid.ui.roi

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import org.blueventures.gemdroid.databinding.MapContainerBinding
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission
import org.blueventures.gemdroid.ui.common.SnackFun

object Polygon {
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        RequestPermission(
            permission = ACCESS_FINE_LOCATION,
            rationale = "This app uses GPS to help zoom the map to your location. Please grant the permission.",
            description = "Please grant permission for the app to use GPS.",
            optional = true
        ) { granted ->
            Layout(viewModel, snack, back, next, granted)
        }
    }

    @Composable
    fun Layout(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click, fineLocation: Boolean) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var polygon: Polygon? = null
            val markers = mutableListOf<Marker>()

            val clearFunc = {
                polygon?.remove()
                for (marker in markers) {
                    marker.remove()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Butt.Text("Clear") {
                    clearFunc()
                    viewModel.clearPoints()
                }
                Butt.Next {
                    if (viewModel.validatePolygon()) {
                        clearFunc()
                        next()
                    } else {
                        snack("Please create a polygon. It's area must be less than 10,000 km². Yours is currently ${"%,d".format(viewModel.polygonArea().toInt())} km²")
                    }
                }
            }

            Map(fineLocation, viewModel, polyGetter = { polygon }, polySetter = { polygon = it }) { markers.add(it) }

            BackHandler {
                clearFunc()
                back()
            }
        }
    }

    @Composable
    fun Map(fineLocation: Boolean, viewModel: RoiViewModel, polyGetter: () -> Polygon?, polySetter: (Polygon) -> Unit, markerAdd: (Marker) -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            var drawing by remember { mutableStateOf(false) }
            AndroidViewBinding(MapContainerBinding::inflate) {
                val mapFragment = mapContainer.getFragment<SupportMapFragment>()
                mapFragment.getMapAsync(MapCallback(fineLocation, viewModel, polyGetter, polySetter, markerAdd) { drawing })
            }

            FloatingActionButton(
                onClick = { drawing = !drawing }, modifier = Modifier
                    .padding(bottom = 64.dp, end = 24.dp)
                    .align(Alignment.BottomEnd)
            ) {
                if (drawing) {
                    Icon(Icons.Filled.Close, "")
                } else {
                    Icon(Icons.Filled.Place, "")
                }
            }
        }
    }

    class MapCallback(
        private val fineLocation: Boolean,
        private val viewModel: RoiViewModel,
        private val polyGetter: () -> Polygon?,
        private val polySetter: (Polygon) -> Unit,
        private val markerAdd: (Marker) -> Unit,
        private val drawingGetter: () -> Boolean,
    ): OnMapReadyCallback {
        @SuppressLint("MissingPermission")
        override fun onMapReady(map: GoogleMap) {
            // despite using clearFunc above whenever navigating away
            // the map still seems to retain markers and polygon, so
            // we clear everything here before adding saved data to the map
            map.clear()

            if (fineLocation) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            }

            for (latlng in viewModel.points) {
                map.addMarker(MarkerOptions().position(latlng))?.let { marker ->
                    markerAdd(marker)
                }
            }

            addPolygon(map, true)

            map.setOnMapClickListener { point ->
                if (drawingGetter()) {
                    map.addMarker(MarkerOptions().position(point))?.let { marker ->
                        viewModel.addPoint(point) { addPolygon(map) }
                        markerAdd(marker)
                    }
                }
            }
        }

        private fun addPolygon(map: GoogleMap, zoom: Boolean = false) {
            viewModel.polygonOpts()?.let { opts ->
                polyGetter()?.remove()
                val poly = map.addPolygon(opts)

                if (zoom) {
                    val builder = LatLngBounds.builder()
                    for (pt in opts.points) {
                        builder.include(pt)
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
                }

                polySetter(poly)
            }
        }
    }
}