package org.blueventures.gemdroid.ui.common.maps

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.databinding.MapContainerBinding
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission
import org.blueventures.gemdroid.ui.common.maps.Draw.DrawingButton

object Maps {
    interface MapImpl<T : URLs> {
        @Composable fun Map(gps: Boolean, tiles: Tiles.Model<T>?, tilesHandler: Tiles.Handler<T>?, draw: Draw.Model?, drawHandler: Draw.Handler?, poly: Poly.Model?, bounds: LatLngBounds?)
    }

    /**
     * The layers argument to Screen should be Layers that outlive composition, such as static fields on an object for example.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T : URLs> Screen(
        floating: @Composable BoxScope.() -> Unit = {},
        attemptGps: Boolean = false,
        tiles: Tiles.Model<T>? = null,
        draw: Draw.Model? = null,
        poly: Poly.Model? = null,
        impl: MapImpl<T> = object : MapImpl<T> {
            @Composable
            override fun Map(
                gps: Boolean,
                tiles: Tiles.Model<T>?,
                tilesHandler: Tiles.Handler<T>?,
                draw: Draw.Model?,
                drawHandler: Draw.Handler?,
                poly: Poly.Model?,
                bounds: LatLngBounds?,
            ) {
                FragmentMap(gps, tiles, tilesHandler, draw, drawHandler, poly, bounds)
            }
        },
    ) {
        if (attemptGps) {
            RequestPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                rationale = stringResource(R.string.gps_rationale),
                description = stringResource(R.string.gps_rationale_description),
                optional = true
            ) { granted ->
                Setup(floating, granted, tiles, draw, poly, impl)
            }
        } else {
            Setup(floating, false, tiles, draw, poly, impl)
        }
    }

    @Composable
    private fun <T : URLs> Setup(floating: @Composable BoxScope.() -> Unit = {}, attemptGps: Boolean = false, tiles: Tiles.Model<T>? = null, draw: Draw.Model? = null, poly: Poly.Model? = null, impl: MapImpl<T>) {
        Tiles.Setup(tiles) { tilesHandler ->
            Draw.Setup(draw) { drawHandler ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val (zoomed, setZoomed) = remember { mutableStateOf(false) }
                    var bounds = shouldZoom(zoomed, tiles, draw, drawHandler)
                    if (!zoomed && bounds != null) {
                        setZoomed(true)
                    } else if (zoomed) {
                        bounds = null
                    }

                    impl.Map(attemptGps, tiles, tilesHandler, draw, drawHandler, poly, bounds)
                    FloatingButton(this, drawHandler, floating)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    private fun <T : URLs> FragmentMap(
        gps: Boolean,
        tiles: Tiles.Model<T>?,
        tilesHandler: Tiles.Handler<T>?,
        draw: Draw.Model?,
        drawHandler: Draw.Handler?,
        poly: Poly.Model?,
        bounds: LatLngBounds?
    ) {
        AndroidViewBinding(MapContainerBinding::inflate) {
            // this call is indeed unsafe
            mapContainer.getFragment<SupportMapFragment>().getMapAsync { map ->
                // we clear everything here before adding saved data to the map
                // because markers and polygons seem to stick around otherwise
                map.clear()

                if (gps) {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                }

                tiles?.let {
                    tilesHandler?.let {
                        Tiles.MapCallback(tiles, tilesHandler).onMapReady(map)
                    }
                }

                poly?.let {
                    Poly.MapCallback(poly).onMapReady(map)
                }

                draw?.let {
                    drawHandler?.let {
                        Draw.MapCallback(draw, drawHandler).onMapReady(map)
                    }
                }

                bounds?.let {
                    zoomToBounds(map, bounds)
                }
            }
        }
    }

    private fun <T : URLs> shouldZoom(zoomed: Boolean, tiles: Tiles.Model<T>?, draw: Draw.Model?, drawHandler: Draw.Handler?): LatLngBounds? {
        return if (drawHandler == null) {
            tiles?.bounds
        } else {
            if (!zoomed) {
                if (draw!!.points().isNotEmpty()) {
                    draw.points()
                } else {
                    tiles?.bounds
                }
            } else {
                null
            }
        }?.let { bounds ->
            boundsFromList(bounds)
        }
    }

    @Composable
    fun FloatingButton(scope: BoxScope, drawHandler: Draw.Handler?, content: @Composable BoxScope.() -> Unit) {
        drawHandler?.let {
            scope.DrawingButton(it)
        } ?: run {
            scope.content()
        }
    }

    fun boundsFromList(list: List<LatLng>): LatLngBounds {
        val builder = LatLngBounds.builder()
        for (pt in list) {
            builder.include(pt)
        }
        return builder.build()
    }

    fun zoomToBounds(map: GoogleMap, bounds: LatLngBounds) = map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

    @Composable
    fun BoxScope.MapActionButton(click: Click, content: @Composable () -> Unit) {
        FloatingActionButton(click, modifier = Modifier
            .padding(bottom = 64.dp, end = 24.dp)
            .align(Alignment.BottomEnd),
            content = content,
        )
    }
}