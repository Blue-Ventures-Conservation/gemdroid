package org.blueventures.gemdroid.ui.common.maps

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Polygon
import kotlinx.coroutines.flow.Flow
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonPolygonFeatureCollection
import org.blueventures.gemdroid.data.Rectangle
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classNumberPropertyKey
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Rad
import org.blueventures.gemdroid.ui.common.Roi.OverviewRow
import org.blueventures.gemdroid.ui.common.SnackFun

data class NamedRectangle(val name: String, override val width: Double, override val height: Double): Rectangle(width, height)
object Capture {
    interface UI {
        val snack: SnackFun
        val done: Click
    }

    interface Data {
        val classes: List<BVClass>
        val capturedCollection: GeojsonPolygonFeatureCollection
        val shapes: Flow<NamedRectangle>
        val captures: Flow<Unit>
        val doneClicks: Flow<Unit>

        fun capture(craClass: BVClass, polygon: List<LatLng>)
    }

    data class Model(private val data: Data, override val snack: SnackFun, override val done: Click): Data by data, UI

    @Composable
    @GoogleMapComposable
    fun Display(model: Model, cameraPositionState: CameraPositionState) {
        var center: LatLng? by remember { mutableStateOf(null) }
        var rect: NamedRectangle? by remember { mutableStateOf(null) }
        val (doCapture, setDoCapture) = remember { mutableStateOf<List<LatLng>?>(null) }
        val (maybeDone, setMaybeDone) = remember { mutableStateOf<Unit?>(null) }
        LaunchedEffect(cameraPositionState) {
            snapshotFlow { cameraPositionState.position.target }
                .collect { center = it }
        }

        val usingSnack = stringResource(R.string.using_s_polygon)
        Effect.Once {
            model.shapes.collect { rectangle ->
                model.snack(usingSnack.format(rectangle.name))
                rect = rectangle
            }
            model.captures.collect {
                optsFromState(center, rect)?.points?.let(setDoCapture)
            }
            model.doneClicks.collect {
                setMaybeDone(Unit)
            }
        }

        doCapture?.let {
            CaptureDialog(model, {
                setDoCapture(null)
            }) {
                model.capture(it, doCapture)
            }
        }

        maybeDone?.let {
            MaybeDoneDialog(model, {
                setMaybeDone(null)
            }) {
                model.done()
            }
        }

        optsFromState(center, rect)?.let { opts ->
            Polygon(points = opts.points, fillColor = Color(opts.fillColor), strokeColor = Color(opts.strokeColor), strokePattern = opts.strokePattern, strokeWidth = opts.strokeWidth, zIndex = 1000f)
        }
    }

    private fun optsFromState(center: LatLng?, rect: NamedRectangle?): PolygonOptions? {
        return if (center != null && rect != null) {
            Rectangle.toPolygon(center, rect)
        }  else null
    }

    @Composable
    fun CaptureDialog(model: Model, onDismiss: Click, onCapture: (BVClass) -> Unit) {
        val (choice, setChoice) = remember { mutableStateOf<BVClass?>(null) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.choose_a_cra_class)) },
            text = {
                Rad.InnerIo(model.classes, choice, setChoice) { bvClass ->
                    bvClass.stringID()
                }
            },
            confirmButton = {
                Butt.Text(stringResource(R.string.capture), choice != null) {
                    choice?.let { onCapture(it) }
                }
            },
            dismissButton = {
                Butt.Text(stringResource(R.string.cancel), click = onDismiss)
            }
        )
    }

    @Composable
    fun MaybeDoneDialog(model: Model, onDismiss: Click, onDone: Click) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.finish_and_upload_your_cras)) },
            text = {
                Text(text = "Your CRAs are as follows:")
                model.classes.forEach { bvClass ->
                    val count = model.capturedCollection.intPropertyCount(classNumberPropertyKey, bvClass.number)
                    OverviewRow(stringResource(bvClass.stringID()) + ": ", "$count")
                }
                Text(text = stringResource(R.string.if_you_are_all_done_click_the_finish_button_below))
            },
            confirmButton = {
                Butt.Text(stringResource(R.string.finished), click = onDone)
            },
            dismissButton = {
                Butt.Text(stringResource(R.string.cancel), click = onDismiss)
            }
        )
    }
}