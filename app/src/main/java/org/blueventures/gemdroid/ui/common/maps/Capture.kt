package org.blueventures.gemdroid.ui.common.maps

import android.content.Context
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Polygon
import kotlinx.coroutines.FlowPreview
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonPolygonFeature
import org.blueventures.gemdroid.data.GeojsonPolygonFeatureCollection
import org.blueventures.gemdroid.data.Rectangle
import org.blueventures.gemdroid.data.analysis.CRAClass
import org.blueventures.gemdroid.model.analysis.cra.CRADatasource.Companion.classNumberPropertyKey
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Rad
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.maps.Maps.MultiMapActionButtons

data class NamedRectangle(val name: String, override val width: Double, override val height: Double): Rectangle(width, height)
object Capture {
    data class State(val shape: NamedRectangle, val center: LatLng? = null, val showGrid: Unit? = null, val doCapture: Unit? = null, val doEdit: String? = null, val maybeDone: Unit? = null)

    interface UI {
        val snack: SnackFun
        val done: Click
        val cellSize: Double

        fun polyModel(state: State, setState: (State) -> Unit): Polygons.Model
        fun currentShape(): NamedRectangle
        fun nextShape(): NamedRectangle
    }

    interface Data {
        val capturedCollection: GeojsonPolygonFeatureCollection

        fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit)
        fun classes(context: Context): List<CRAClass>
        fun capture(craClass: CRAClass, polygon: List<LatLng>, callback: (Result<Unit>) -> Unit)
        fun identity(feature: GeojsonPolygonFeature): String?
        fun currentClass(id: String): String?
        fun updateClass(id: String, newClass: CRAClass, callback: (Result<Unit>) -> Unit)
        fun delete(id: String, callback: (Result<Unit>) -> Unit)
    }

    @OptIn(FlowPreview::class)
    data class Model(private val data: Data, private val usesS2: Boolean, override val snack: SnackFun, override val done: Click): Data by data, UI {
        private val scale = if (usesS2) 10.0 else 30.0
        private var shapeCursor = 0
        private val shapeOrder =  listOf(
            NamedRectangle("3x3", scale*3, scale*3),
            NamedRectangle("2x2", scale*2, scale*2),
            NamedRectangle("3x2", scale*3, scale*2),
            NamedRectangle("2x3", scale*2, scale*3),
            NamedRectangle("4x2", scale*4, scale*2),
            NamedRectangle("2x4", scale*2, scale*4),
            NamedRectangle("6x1", scale*6, scale*1),
            NamedRectangle("1x6", scale*1, scale*6)
        )
        override val cellSize = scale

        override fun polyModel(state: State, setState: (State) -> Unit): Polygons.Model {
            return object : Polygons.Model() {
                override val touchEnabled = true
                override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) = data.polygonGroups(context, callback)
                override fun onTouch(point: Polygons.NamedPoint?) = @Composable { if (point != null) setState(state.copy(doEdit = point.name)) }
            }
        }
        override fun currentShape() = shapeOrder[shapeCursor]
        override fun nextShape(): NamedRectangle {
            shapeCursor = (shapeCursor + 1) % shapeOrder.size
            return shapeOrder[shapeCursor]
        }
    }

    @Composable
    fun prepareState(model: Model, cameraPositionState: CameraPositionState): Pair<State, (State) -> Unit> {
        val (state, setState) = remember { mutableStateOf(State(model.currentShape())) }
        LaunchedEffect(cameraPositionState) {
            snapshotFlow { cameraPositionState.position.target }
                .collect { setState(state.copy(center = it)) }
        }
        return Pair(state, setState)
    }

    @Composable
    @GoogleMapComposable
    fun Display(model: Model, state: State, setState: (State) -> Unit, setGroups: (List<Polygons.NamedOptionsGroup>?) -> Unit) {
        val context = LocalContext.current.applicationContext
        state.doCapture?.let {
            val onDismiss = { setState(state.copy(doCapture = null)) }
            CaptureDialog(model, onDismiss) { craClass ->
                optsFromState(model.cellSize, state.center, state.shape)?.first?.points?.let { polygon ->
                    model.capture(craClass, polygon) { result ->
                        when {
                            result.isFailure -> model.snack(context.getString(R.string.failed_to_save_please_try_again))
                            else -> {
                                onDismiss()
                                setGroups(null)
                            }
                        }
                    }
                }
            }
        }

        state.doEdit?.let { stringID ->
            val onDismiss = { setState(state.copy(doEdit = null)) }
            val callback: (Result<Unit>) -> Unit = { result ->
                when {
                    result.isFailure -> model.snack(context.getString(R.string.failed_to_save_please_try_again))
                    else -> {
                        onDismiss()
                        setGroups(null)
                    }
                }
            }
            EditDialog(model, model.currentClass(stringID), onDismiss, { newClass ->
                model.updateClass(stringID, newClass, callback)
            }) {
                model.delete(stringID, callback)
            }
        }

        state.maybeDone?.let {
            val onDismiss = {
                setState(state.copy(maybeDone = null))
            }
            MaybeDoneDialog(model, onDismiss) {
                onDismiss()
                model.done()
            }
        }

        optsFromState(model.cellSize, state.center, state.shape)?.let { optsPair ->
            val opts = optsPair.first
            DrawMapPolygon(opts)
            state.showGrid?.let {
                val gridOpts = optsPair.second
                gridOpts.forEach { opts ->
                    DrawMapPolygon(opts)
                }
            }
        }
    }

    @GoogleMapComposable
    @Composable
    private fun DrawMapPolygon(opts: PolygonOptions) {
        Polygon(points = opts.points, fillColor = Color(opts.fillColor), strokeColor = Color(opts.strokeColor), strokePattern = opts.strokePattern, strokeWidth = opts.strokeWidth, zIndex = 1000f)
    }

    private fun optsFromState(cellSize: Double, center: LatLng?, rect: NamedRectangle?): Pair<PolygonOptions, List<PolygonOptions>>? {
        return if (center != null && rect != null) {
            Rectangle.toPolygon(cellSize, center, rect)
        }  else null
    }

    @Composable
    fun BoxScope.CaptureMapActions(model: Model, state: State, setState: (State) -> Unit) {
        val usingSnack = stringResource(R.string.using_s_polygon)
        val createCRAsFirst = stringResource(R.string.please_create_some_cras_before_tapping_the_done_button)
        val gridShowing = state.showGrid != null
        MultiMapActionButtons(ClickContent({
            setState(state.copy(doCapture = Unit))
        }) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = stringResource(R.string.capture_the_current_area))
        }, ClickContent({
            val nextRect = model.nextShape()
            model.snack(usingSnack.format(nextRect.name))
            setState(state.copy(shape = nextRect))
        }) {
            Icon(Icons.Filled.FormatShapes, contentDescription = stringResource(R.string.modify_polygon_shape))
        }, ClickContent({
            setState(state.copy(showGrid = if (gridShowing) null else Unit))
        }) {
            Icon(if (gridShowing) Icons.Filled.GridOff else Icons.Filled.GridOn, contentDescription = stringResource(R.string.toggle_the_inner_grid_of_the_capture_polygon_on_or_off))
        }, ClickContent({
            if (model.capturedCollection.features.isNotEmpty()) {
                setState(state.copy(maybeDone = Unit))
            } else {
                model.snack(createCRAsFirst)
            }
        }) {
            Icon(Icons.Filled.DoneAll, contentDescription = stringResource(R.string.finished_creating_cras))
        })
    }

    @Composable
    fun EditDialog(model: Model, currentClass: String?, onDismiss: Click, onEdit: (CRAClass) -> Unit, onDelete: () -> Unit) {
        val classChoices = model.classes(LocalContext.current).toMutableList()
        var initChoice: CRAClass? = null
        for (craClass in classChoices) {
            if (currentClass == craClass.name) {
                initChoice = craClass
                break
            }
        }

        val (choice, setChoice) = remember { mutableStateOf(initChoice) }
        val deleteClassNumber = -999999
        classChoices.add(CRAClass(deleteClassNumber, stringResource(R.string.delete)))
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.change_cra_class_or_delete)) },
            text = { ClassChoices(classChoices, choice, setChoice) },
            confirmButton = {
                Butt.Text(stringResource(R.string.finished), choice != null) {
                    choice?.let { chosen ->
                        if (chosen.number == deleteClassNumber) {
                            onDelete()
                        } else {
                            onEdit(chosen)
                        }
                    }
                }
            },
            dismissButton = { Butt.Text(stringResource(R.string.cancel), click = onDismiss) }
        )
    }

    @Composable
    fun CaptureDialog(model: Model, onDismiss: Click, onCapture: (CRAClass) -> Unit) {
        val (choice, setChoice) = remember { mutableStateOf<CRAClass?>(null) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.choose_a_cra_class)) },
            text = { ClassChoices(model.classes(LocalContext.current), choice, setChoice) },
            confirmButton = {
                Butt.Text(stringResource(R.string.capture), choice != null) {
                    choice?.let { onCapture(it) }
                }
            },
            dismissButton = { Butt.Text(stringResource(R.string.cancel), click = onDismiss) }
        )
    }

    @Composable
    fun ClassChoices(classes: List<CRAClass>, choice: CRAClass?, setChoice: (CRAClass?) -> Unit) {
        Column(modifier = Modifier
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState())) {
            Rad.InnerIo(classes, choice, setChoice) { _, craClass ->  craClass.name }
        }
    }

    @Composable
    fun MaybeDoneDialog(model: Model, onDismiss: Click, onDone: Click) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.finish_and_upload_your_cras)) },
            text = {
                Column(modifier = Modifier
                    .padding(bottom = 16.dp)
                    .verticalScroll(rememberScrollState())) {
                    Text(text = stringResource(R.string.your_cras_are_as_follows))
                    model.classes(LocalContext.current).forEach { craClass ->
                        val count = model.capturedCollection.intPropertyCount(classNumberPropertyKey, craClass.number)
                        Info.Row {
                            Info.Txt(craClass.name + ": ", 14.sp, truncate = true)
                            Info.Txt("$count", 14.sp)
                        }
                    }
                    Text(text = stringResource(R.string.if_you_are_all_done_click_the_finish_button_below))
                }
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