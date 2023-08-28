package org.blueventures.gemdroid.ui.common.maps

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Stale
import org.blueventures.gemdroid.databinding.MapContainerBinding
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission
import org.blueventures.gemdroid.ui.common.maps.Draw.DrawingButton

object Maps {
    /**
     * The layers argument to Screen should be Layers that outlive composition, such as static fields on an object for example.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T : Stale> Screen(
        floatingContent: @Composable BoxScope.() -> Unit = {},
        attemptGps: Boolean = false,
        tiles: Tiles.Model<T>? = null,
        draw: Draw.Model? = null,
        poly: Poly.Model? = null
    ) {
        if (attemptGps) {
            RequestPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                rationale = stringResource(R.string.gps_rationale),
                description = stringResource(R.string.gps_rationale_description),
                optional = true
            ) { granted ->
                Layout(floatingContent, granted, tiles, draw, poly)
            }
        } else {
            Layout(floatingContent, false, tiles, draw, poly)
        }
    }

    @Composable
    private fun <T : Stale> Layout(
        floatingContent: @Composable BoxScope.() -> Unit,
        fineLocation: Boolean,
        tiles: Tiles.Model<T>?,
        draw: Draw.Model?,
        poly: Poly.Model?
    ){
        Tiles.Setup(tiles) { urls ->
            Draw.Setup(draw) { drawing ->
                Map(floatingContent, fineLocation, tiles, urls, draw, drawing, poly)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    private fun <T : Stale> Map(
        floatingContent: @Composable BoxScope.() -> Unit,
        fineLocation: Boolean,
        tiles: Tiles.Model<T>?,
        urls: Tiles.UrlHandler<T>?,
        draw: Draw.Model?,
        drawing: Draw.DrawingHandler?,
        poly: Poly.Model?
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidViewBinding(MapContainerBinding::inflate) {
                val mapFragment = mapContainer.getFragment<SupportMapFragment>()
                mapFragment.getMapAsync { map ->
                    // we clear everything here before adding saved data to the map
                    // because markers and polygons seem to stick around otherwise
                    map.clear()

                    if (fineLocation) {
                        map.isMyLocationEnabled = true
                        map.uiSettings.isMyLocationButtonEnabled = true
                    }

                    tiles?.let {
                        urls?.let {
                            Tiles.MapCallback(tiles, urls).onMapReady(map)
                        }
                    }

                    poly?.let {
                        Poly.MapCallback(poly).onMapReady(map)
                    }

                    draw?.let {
                        drawing?.let {
                            Draw.MapCallback(draw, drawing).onMapReady(map)
                        }
                    }

                    if (drawing != null && draw!!.points().isNotEmpty()) {
                        draw.points()
                    } else {
                        tiles?.bounds
                    }?.let {
                        zoomToBounds(map, it)
                    }
                }
            }

            drawing?.let {
                DrawingButton(it)
            } ?: run {
                this.floatingContent()
            }
        }
    }

    fun zoomToBounds(map: GoogleMap, bounds: List<LatLng>) {
        val builder = LatLngBounds.builder()
        for (pt in bounds) {
            builder.include(pt)
        }
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
    }

    @Composable
    fun BoxScope.MapActionButton(click: Click, content: @Composable () -> Unit) {
        FloatingActionButton(click, modifier = Modifier
            .padding(bottom = 64.dp, end = 24.dp)
            .align(Alignment.BottomEnd),
            content = content,
        )
    }
}