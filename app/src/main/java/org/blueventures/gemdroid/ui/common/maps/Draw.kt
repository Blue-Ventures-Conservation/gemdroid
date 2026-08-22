package org.blueventures.gemdroid.ui.common.maps

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Polygon
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.ClickContent
import org.blueventures.gemdroid.ui.common.FloatingButtons
import org.blueventures.gemdroid.ui.common.SnackFun
import kotlin.math.roundToInt

object Draw {
    interface UI {
        val snack: SnackFun
        val next: Click
    }

    interface Data {
        var drawnPoints: List<LatLng>
    }

    data class Model(private val data: Data, override val snack: SnackFun, override val next: Click): Data by data, UI

    enum class MotionEvent { IDLE, DOWN, UP, MOVE }
    data class MapPolygonState(
        val currentPosition: Offset = Offset.Unspecified,
        val event: MotionEvent = MotionEvent.IDLE
    )

    data class DrawState(val drawing: Boolean = false, val screenPoints: List<Point> = emptyList())

    @Composable
    fun prepareState(): MutableState<DrawState> {
        val drawState = remember { mutableStateOf(DrawState()) }
        return drawState
    }

    @Composable
    @GoogleMapComposable
    fun Display(model: Model, drawState: MutableState<DrawState>, camera: CameraPositionState) {
        if (drawState.value.screenPoints.isNotEmpty()) {
            camera.projection?.let {  proj ->
                model.drawnPoints = drawState.value.screenPoints.map {
                    proj.fromScreenLocation(it)
                }
                PolygonUtils.opt(listOf(model.drawnPoints))?.let { opt ->
                    Polygon(points = opt.points, fillColor = Color(opt.fillColor), strokeColor = Color(opt.strokeColor), strokePattern = opt.strokePattern, strokeWidth = opt.strokeWidth, zIndex = 100f)
                }
            }
        }
    }

    @Composable
    fun BoxScope.DrawMapActions(model: Model, drawState: MutableState<DrawState>) {
        if (drawState.value.drawing) {
            Canvas { screenPoints ->
                drawState.value = drawState.value.copy(screenPoints = screenPoints)
            }
        }

        val pleaseDraw = stringResource(R.string.please_create_polygon)
        val tooBig = stringResource(R.string.polygon_sizing)
        FloatingButtons(ClickContent({
            drawState.value = drawState.value.copy(drawing = !drawState.value.drawing)
        }) {
            if (drawState.value.drawing) {
                Icon(Icons.Filled.Close, stringResource(R.string.start_drawing_polygon))
            } else {
                Icon(Icons.Filled.Draw, stringResource(R.string.stop_drawing_polygon))
            }
        }, ClickContent({
            model.drawnPoints = emptyList()
            drawState.value = DrawState(false, emptyList())
        }) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.clear_the_currently_drawn_polygon))
        }, ClickContent({
            if (PolygonUtils.validate(model.drawnPoints)) {
                model.next()
            } else {
                val msg = if (PolygonUtils.ringAreaHectares(model.drawnPoints) <= 0) {
                    pleaseDraw
                } else {
                    val max = PolygonUtils.maxHectaresString()
                    val current = PolygonUtils.areaStr(listOf(model.drawnPoints))
                    String.format(tooBig, max, current)
                }

                model.snack(msg)
            }
        }) {
            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.i_m_done_drawing_let_s_go_to_the_next_screen))
        })
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