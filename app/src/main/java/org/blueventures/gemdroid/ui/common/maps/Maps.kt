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
import org.blueventures.gemdroid.databinding.MapContainerBinding
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Poly.DrawingButton

object Maps {
    /**
     * The layers argument to Screen should be Layers that outlive composition, such as static fields on an object for example.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T> Screen(
        title: String = "",
        appBar: AppBarFun = {},
        snack: SnackFun = {},
        back: Click = {},
        next: Click = {},
        floatingContent: @Composable BoxScope.() -> Unit = {},
        attemptGps: Boolean = false,
        tiles: Tiles.Model<T>? = null,
        poly: Poly.Model? = null
    ) {
        if (attemptGps) {
            RequestPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                rationale = stringResource(R.string.gps_rationale),
                description = stringResource(R.string.gps_rationale_description),
                optional = true
            ) { granted ->
                Layout(title, appBar, snack, back, next, floatingContent, granted, tiles, poly)
            }
        } else {
            Layout(title, appBar, snack, back, next, floatingContent, false, tiles, poly)
        }
    }

    @Composable
    private fun <T> Layout(
        title: String,
        appBar: AppBarFun,
        snack: SnackFun,
        back: Click,
        next: Click,
        floatingContent: @Composable BoxScope.() -> Unit,
        fineLocation: Boolean,
        tiles: Tiles.Model<T>?,
        poly: Poly.Model?
    ){
        Tiles.Setup(title, appBar, tiles) { urls ->
            var bounds = tiles?.bounds
            Poly.Setup(poly, snack, back, next) { drawing ->
                if (drawing != null && poly!!.points().isNotEmpty()) {
                    bounds = poly.points()
                }

                Map(floatingContent, fineLocation, bounds, tiles, urls, poly, drawing)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    private fun <T> Map(
        floatingContent: @Composable BoxScope.() -> Unit,
        fineLocation: Boolean,
        bounds: List<LatLng>?,
        tilesModel: Tiles.Model<T>?,
        urls: Tiles.UrlHandler<T>?,
        drawer: Poly.Model?,
        drawing: Poly.DrawingHandler?
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

                    tilesModel?.let { tiles ->
                        urls?.let { u ->
                            Tiles.MapCallback(tiles, u).onMapReady(map)
                        }
                    }

                    drawer?.let {
                        drawing?.let {
                            Poly.MapCallback(drawer, drawing).onMapReady(map)
                        }
                    }

                    bounds?.let {
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