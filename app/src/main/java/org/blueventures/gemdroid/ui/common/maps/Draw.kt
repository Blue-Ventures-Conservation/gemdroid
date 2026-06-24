package org.blueventures.gemdroid.ui.common.maps

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Polygon
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import kotlin.math.roundToInt

object Draw {
    interface UI {
        val snack: SnackFun
        val next: Click
    }

    interface Data {
        val maxPoints: Int
        var points: List<LatLng>
        fun clear()
        fun polygonOptions(): PolygonOptions?

        /** validation functions */
        fun validatePolygon(): Boolean
        fun maxHectares(): String
        fun polygonHectares(): String
        fun area(): Double
    }

    data class Model(private val drawPoly: PolygonDrawer, override val snack: SnackFun, override val next: Click): Data by drawPoly, UI

    enum class MotionEvent { IDLE, DOWN, UP, MOVE }
    data class MapPolygonState(
        val currentPosition: Offset = Offset.Unspecified,
        val event: MotionEvent = MotionEvent.IDLE
    )

    @Composable
    fun BoxScope.DrawButton(draw: MutableState<Boolean>) {
        MapActionButton({
            draw.value = !draw.value
        }) {
            if (draw.value) {
                Icon(Icons.Filled.Close, stringResource(R.string.stop_drawing_polygon))
            } else {
                Icon(Icons.Filled.Draw, stringResource(R.string.start_drawing_polygon))
            }
        }
    }

    @Composable
    @GoogleMapComposable
    fun Do(draw: Model, screenPoints: List<Point>, camera: CameraPositionState, clearState: MutableState<Boolean>) {
        if (screenPoints.isNotEmpty()) {
            camera.projection?.let {  proj ->
                draw.points = screenPoints.map {
                    proj.fromScreenLocation(it)
                }
            }
        }

        draw.polygonOptions()?.let { opt ->
            Polygon(points = opt.points, fillColor = Color(opt.fillColor), strokeColor = Color(opt.strokeColor), strokePattern = opt.strokePattern, strokeWidth = opt.strokeWidth, zIndex = 100f)
        }

        if (clearState.value) {
            draw.clear()
            clearState.value = false
        }
    }

    @Composable
    fun Canvas(onDrawingEnd : (List<Point>) -> Unit) {
        var state by remember { mutableStateOf(MapPolygonState()) }
        val brush = remember { SolidColor(Color.Green) }
        var path = remember { Path() }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val screenPoints = mutableListOf<Point>()
                        awaitPointerEvent().changes
                            .first()
                            .also { changes ->
                                val position = changes.position
                                screenPoints.add(position.toPoint())
                                state = state.copy(
                                    currentPosition = position,
                                    event = MotionEvent.DOWN
                                )
                            }
                        do {
                            val event: PointerEvent = awaitPointerEvent()
                            event.changes.forEach { changes ->
                                val position = changes.position
                                screenPoints.add(position.toPoint())
                                state = state.copy(
                                    currentPosition = position,
                                    event = MotionEvent.MOVE
                                )
                            }
                        } while (event.changes.any { it.pressed })

                        currentEvent.changes
                            .first()
                            .also { change ->
                                state = state.copy(
                                    currentPosition = change.position,
                                    event = MotionEvent.UP
                                )
                                screenPoints.add(change.position.toPoint())
                            }
                        val next = Path()
                        next.moveTo(state.currentPosition.x, state.currentPosition.y)
                        path = next
                        onDrawingEnd(screenPoints)
                    }
                },
            onDraw = {
                when (state.event) {
                    MotionEvent.IDLE -> Unit
                    MotionEvent.UP, MotionEvent.MOVE -> path.lineTo(
                        state.currentPosition.x,
                        state.currentPosition.y
                    )

                    MotionEvent.DOWN -> path.moveTo(
                        state.currentPosition.x,
                        state.currentPosition.y
                    )
                }
                drawPath(
                    path = path,
                    brush = brush,
                    style = Stroke(width = 8f)
                )
            }
        )
    }

    private fun Offset.toPoint(): Point {
        return Point(x.roundToInt(), y.roundToInt())
    }
}