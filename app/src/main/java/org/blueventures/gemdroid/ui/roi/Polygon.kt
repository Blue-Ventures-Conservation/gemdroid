package org.blueventures.gemdroid.ui.roi

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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
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
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun

object Polygon {
    @Composable
    fun Screen(viewModel: RoiViewModel, snack: SnackFun, back: Click, next: Click) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var polygon: Polygon? = null
            val markers = arrayListOf<Marker>()

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
                Button(onClick = {
                    clearFunc()
                    viewModel.clearPoints()
                }) {
                    Text(text = "Clear", fontSize = 20.sp)
                }
                Button(onClick = {
                    if (viewModel.validatePolygon()) {
                        clearFunc()
                        next()
                    } else {
                        snack("Please create a polygon. It's area must be less than 10,000 km². Yours is currently ${"%,d".format(viewModel.polygonArea().toInt())} km²")
                    }
                }) {
                    Text(text = "Next", fontSize = 20.sp)
                }
            }

            Map(viewModel, polyGetter = { polygon }, polySetter = { polygon = it }, markerAdd = { markers.add(it) })

            BackHandler {
                clearFunc()
                back()
            }
        }
    }

    @Composable
    fun Map(viewModel: RoiViewModel, polyGetter: () -> Polygon?, polySetter: (Polygon) -> Unit, markerAdd: (Marker) -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            var drawing by remember { mutableStateOf(false) }
            AndroidViewBinding(MapContainerBinding::inflate) {
                val mapFragment = mapContainer.getFragment<SupportMapFragment>()
                mapFragment.getMapAsync(MapCallback(viewModel, polyGetter, polySetter, markerAdd) { drawing })
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
        val viewModel: RoiViewModel,
        val polyGetter: () -> Polygon?,
        val polySetter: (Polygon) -> Unit,
        val markerAdd: (Marker) -> Unit,
        val drawingGetter: () -> Boolean,
    ): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            // despite using clearFunc above whenever navigating away
            // the map still seems to retain markers and polygon, so
            // we clear everything here before added saved data to the map
            map.clear()

            for (latlng in viewModel.points) {
                map.addMarker(MarkerOptions().position(latlng))?.let { marker ->
                    markerAdd(marker)
                }
            }

            addPolygon(map, true)

            map.setOnMapClickListener { point ->
                if (drawingGetter()) {
                    map.addMarker(MarkerOptions().position(point))?.let { marker ->
                        viewModel.addPoint(point)
                        markerAdd(marker)
                        addPolygon(map)
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