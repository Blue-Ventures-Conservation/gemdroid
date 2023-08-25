package org.blueventures.gemdroid.ui.common.maps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton

object Draw {
    abstract class Model {
        val markers = mutableListOf<Marker>()
        var polygon: Polygon? = null

        abstract fun polygonOptions(): PolygonOptions?
        abstract fun addPoint(point: LatLng, callback: () -> Unit): Job
        abstract fun validatePolygon(): Boolean
        abstract fun polygonIterate(mapf: (LatLng) -> Unit)
        abstract fun maxSquareKms(): String
        abstract fun polygonSquareKms(): String
        abstract fun points(): List<LatLng>
        abstract fun clearPoints()
        fun clearMapObjects() {
            polygon?.remove()
            for (marker in markers) marker.remove()
        }
    }

    @Composable
    fun Setup(
        model: Model?,
        snack: SnackFun,
        next: Click,
        back: Click,
        content: @Composable (DrawingHandler?) -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (drawing, setDrawing) = remember { mutableStateOf(false) }

            model?.let { draw ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Butt.Text(stringResource(R.string.clear)) {
                        draw.clearMapObjects()
                        draw.clearPoints()
                    }
                    val snackStr = stringResource(R.string.polygon_sizing)
                    Butt.Next {
                        if (draw.validatePolygon()) {
                            draw.clearMapObjects()
                            next()
                        } else {
                            val max = draw.maxSquareKms()
                            val current = draw.polygonSquareKms()
                            snack(snackStr.format(max, current))
                        }
                    }
                }

                content(object: DrawingHandler {
                    override fun drawing() = drawing
                    override fun setDrawing(drawing: Boolean) = setDrawing(drawing)
                })
            } ?: run {
                content(null)
            }

            BackHandler {
                model?.clearMapObjects()
                back()
            }
        }
    }

    class MapCallback(private val model: Model, private val drawing: DrawingHandler): OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            model.polygonIterate {
                map.addMarker(MarkerOptions().position(it))?.let { marker ->
                    model.markers.add(marker)
                }
            }

            addPolygon(map, true)

            map.setOnMapClickListener { point ->
                if (drawing.drawing()) {
                    map.addMarker(MarkerOptions().position(point))?.let { marker ->
                        model.addPoint(point) { addPolygon(map) }
                        model.markers.add(marker)
                    }
                }
            }
        }

        private fun addPolygon(map: GoogleMap, zoom: Boolean = false) {
            model.polygonOptions()?.let { opts ->
                model.polygon?.remove()
                model.polygon = map.addPolygon(opts)
                if (zoom) {
                    Maps.zoomToBounds(map, opts.points)
                }
            }
        }
    }

    interface DrawingHandler {
        fun drawing(): Boolean
        fun setDrawing(drawing: Boolean)
    }

    @Composable
    fun BoxScope.DrawingButton(drawing: DrawingHandler) {
        MapActionButton({ drawing.setDrawing(!drawing.drawing()) }) {
            if (drawing.drawing()) {
                Icon(Icons.Filled.Close, stringResource(R.string.stop_drawing_polygon))
            } else {
                Icon(Icons.Filled.Place, stringResource(R.string.place_polygon_corner))
            }
        }
    }
}