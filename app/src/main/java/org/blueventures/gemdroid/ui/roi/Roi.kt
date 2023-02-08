package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.chargemap.compose.numberpicker.NumberPicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import org.blueventures.gemdroid.databinding.FragmentContainerBinding
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.ui.theme.SkyBlue
import java.io.File

object Roi {
    @Composable
    fun List(viewModel: RoiViewModel, filesDir: File, snackbar: (String) -> Unit, roiClick: (File) -> Unit, floatingOnClick: () -> Unit) {
        val state by viewModel.state.collectAsState()
        val (toDelete, setDeleteRoi) = remember{ mutableStateOf<File?>(null) }

        if (toDelete != null) {
            DeleteDialog(viewModel, snackbar, toDelete) { setDeleteRoi(null) }
        }

        if (state.rois == null) {
            Progress()
            viewModel.refreshRois(filesDir)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = floatingOnClick, modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(Icons.Filled.Add, "Add new ROI")
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Regions of Interest", fontSize = 24.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 24.dp, bottom = 32.dp)
                    )
                    Divider(color = SkyBlue, thickness = 4.dp)
                    ListView(viewModel, roiClick, setDeleteRoi)
                }
            }
        }
    }

    @Composable
    fun DeleteDialog(viewModel: RoiViewModel, snackbar: (String) -> Unit, toDelete: File, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Delete ROI") },
            text = { Text(text = "Really delete '${toDelete.name}'?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteRoi(toDelete) { success ->
                        onDismiss()
                        if (!success) {
                            snackbar("Failed to delete ${toDelete.name}")
                        }
                    }
                }) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                Button(onClick = {
                    onDismiss()
                }) {
                    Text(text = "Cancel")
                }
            },
        )
    }

    @Composable
    fun ListView(viewModel: RoiViewModel, roiClick: (File) -> Unit, setDeleteRoi: (File?) -> Unit) {
        val state by viewModel.state.collectAsState()
        state.rois?.let { rois ->
            if (rois.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Regions of Interest (ROIs) yet, create one by tapping the plus button!",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    items(rois) { dir ->
                        RoiRow(dir, roiClick, setDeleteRoi)
                        Divider(color = SkyBlue, thickness = 1.dp)
                    }
                }
            }
        }
    }

    @Composable
    fun RoiRow(dir: File, roiClick: (File) -> Unit, setDeleteRoi: (File?) -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { roiClick(dir) },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = dir.name, fontSize = 24.sp, modifier = Modifier.padding(24.dp))
            Icon(Icons.Filled.Delete, "Delete ROI", modifier = Modifier
                .padding(20.dp)
                .size(32.dp)
                .clickable {
                    setDeleteRoi(dir)
                })
        }
    }

    @Composable
    fun Name(viewModel: RoiViewModel, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Name your ROI", fontSize = 24.sp, textAlign = TextAlign.Center)
            NameField(viewModel = viewModel)
            NameButton {
                val name = viewModel.state.value.name
                if (name.isNotEmpty() && viewModel.isUnique(name)) {
                    viewModel.setName(name)
                    nextClick()
                } else {
                    snackbar("Please enter a unique name.")
                }
            }
        }

        BackHandler {
            backClick()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NameField(viewModel: RoiViewModel) {
        val focus = LocalFocusManager.current
        var text by remember { mutableStateOf(viewModel.state.value.name) }

        TextField(
            value = text,
            onValueChange = {  viewModel.setName(it); text = it },
            label = { Text("Please enter a name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Words),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            textStyle = TextStyle.Default.copy(fontSize = 24.sp)
        )
    }

    @Composable
    fun NameButton(nextClick: () -> Unit) {
        Button(
            onClick = { nextClick() }
        ) {
            Text("Next", fontSize = 18.sp)
        }
    }

    @Composable
    fun ContemporaryDates(viewModel: RoiViewModel, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select bounding years (inclusive) for contemporary imagery:", textAlign = TextAlign.Center, fontSize = 24.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContemporaryYearStart(viewModel)
                ContemporaryYearEnd(viewModel)
            }
            Button(
                onClick = {
                    if (viewModel.validateContemporaryYearsOrder()) {
                        if (viewModel.validateContemporaryYearsGap()) {
                            nextClick()
                        } else {
                            snackbar("Please select years less than ${RoiViewModel.maxYearGap} years apart")
                        }
                    } else {
                        snackbar("Year on the left must be equal to or less than the one on right")
                    }
                }
            ) {
                Text("Next", fontSize = 18.sp)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun ContemporaryYearStart(viewModel: RoiViewModel) {
        var year by remember { mutableStateOf(viewModel.state.value.contemporaryYearStart) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..viewModel.currentYear(),
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setContemporaryYearStart(it)
                year = it
            }
        )
    }

    @Composable
    fun ContemporaryYearEnd(viewModel: RoiViewModel) {
        var year by remember { mutableStateOf(viewModel.state.value.contemporaryYearEnd) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..viewModel.currentYear(),
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setContemporaryYearEnd(it)
                year = it
            }
        )
    }

    @Composable
    fun HistoricalDates(viewModel: RoiViewModel, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select bounding years (inclusive) for historical imagery:", textAlign = TextAlign.Center, fontSize = 24.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoricalYearStart(viewModel)
                HistoricalYearEnd(viewModel)
            }
            Button(
                onClick = {
                    if (viewModel.validateHistoricalYearsOrder()) {
                        if (viewModel.validateHistoricalYearsGap()) {
                            nextClick()
                        } else {
                            snackbar("Please select years less than ${RoiViewModel.maxYearGap} years apart")
                        }
                    } else {
                        snackbar("Year on the left must be equal to or less than the one on right")
                    }
                }
            ) {
                Text("Next", fontSize = 18.sp)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun HistoricalYearStart(viewModel: RoiViewModel) {
        var year by remember { mutableStateOf(viewModel.state.value.historicalYearStart) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..viewModel.currentYear(),
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setHistoricalYearStart(it)
                year = it
            }
        )
    }

    @Composable
    fun HistoricalYearEnd(viewModel: RoiViewModel) {
        var year by remember { mutableStateOf(viewModel.state.value.historicalYearEnd) }
        NumberPicker(
            value = year,
            range = RoiViewModel.oldestLandsatYear..viewModel.currentYear(),
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setHistoricalYearEnd(it)
                year = it
            }
        )
    }

    @Composable
    fun Months(viewModel: RoiViewModel, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select range of months (inclusive) for imagery:", textAlign = TextAlign.Center, fontSize = 24.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthStart(viewModel)
                MonthEnd(viewModel)
            }
            Button(
                onClick = {
                    if (viewModel.validateMonthsOrder()) {
                        nextClick()
                    } else {
                        snackbar("Month on the left must be equal to or less than the one on the right")
                    }
                }
            ) {
                Text("Next", fontSize = 18.sp)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun MonthStart(viewModel: RoiViewModel) {
        var month by remember { mutableStateOf(viewModel.state.value.monthStart) }
        NumberPicker(
            value = month,
            range = 1..12,
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setMonthStart(it)
                month = it
            }
        )
    }

    @Composable
    fun MonthEnd(viewModel: RoiViewModel) {
        var month by remember { mutableStateOf(viewModel.state.value.monthEnd) }
        NumberPicker(
            value = month,
            range = 1..12,
            dividersColor = MaterialTheme.colorScheme.primary,
            textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
            onValueChange = {
                viewModel.setMonthEnd(it)
                month = it
            }
        )
    }


    @Composable
    fun Indices(viewModel: RoiViewModel, backClick: () -> Unit, nextClick: () -> Unit) {
        val choices = viewModel.getIndices()
        val (choice, setChoice) = remember { mutableStateOf(viewModel.state.value.indices) }
        Column(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Spectral Indices", fontSize = 32.sp)
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                choices.forEach { indices ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (indices == choice),
                                onClick = {
                                    setChoice(indices)
                                }
                            )
                            .padding(top = 24.dp, bottom = 24.dp)
                    ) {
                        RadioButton(selected = (indices == choice), onClick = { setChoice(indices) })
                        Text(text = indices.toLabel(), modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
            Button(
                onClick = {
                    viewModel.setIndices(choice)
                    nextClick()
                }
            ) {
                Text("Next", fontSize = 18.sp)
            }
        }

        BackHandler {
            backClick()
        }
    }

    @Composable
    fun Polygon(viewModel: RoiViewModel, snackbar: (String) -> Unit, backClick: () -> Unit, nextClick: () -> Unit) {
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
                    Text(text = "Clear", fontSize = 18.sp)
                }
                Button(onClick = {
                    if (viewModel.validatePolygon()) {
                        clearFunc()
                        nextClick()
                    } else {
                        snackbar("Please create a polygon. It's area must be less than 10,000 km². Yours is currently ${"%,d".format(viewModel.polygonArea().toInt())} km²")
                    }
                }) {
                    Text(text = "Next", fontSize = 18.sp)
                }
            }

            Map(viewModel, polyGetter = { polygon }, polySetter = { polygon = it }, markerAdd = { markers.add(it) })

            BackHandler {
                clearFunc()
                backClick()
            }
        }
    }

    @Composable
    fun Map(viewModel: RoiViewModel, polyGetter: () -> Polygon?, polySetter: (Polygon) -> Unit, markerAdd: (Marker) -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            var drawing by remember { mutableStateOf(false) }
            AndroidViewBinding(FragmentContainerBinding::inflate) {
                val mapFragment = fragmentContainer.getFragment<SupportMapFragment>()
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

    @Composable
    fun Overview(viewModel: RoiViewModel, filesDir: File, snackbar: (String) -> Unit, doneClick: () -> Unit) {
        val state by viewModel.state.collectAsState()
        if (state.saving) {
            Progress()
        } else {
            OverviewDetails(viewModel, filesDir, snackbar, doneClick)
        }
    }

    @Composable
    fun OverviewDetails(viewModel: RoiViewModel, filesDir: File, snackbar: (String) -> Unit, doneClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Overview", fontSize = 32.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val state by viewModel.state.collectAsState()
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "Name: ", fontSize = 18.sp)
                    Text(text = "Contemporary Years: ", fontSize = 18.sp)
                    Text(text = "Historical Years: ", fontSize = 18.sp)
                    Text(text = "Months: ", fontSize = 18.sp)
                    Text(text = "Polygon ROI: ", fontSize = 18.sp)
                    Text(text = "Spectral Indices: ", fontSize = 18.sp)
                }
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = state.name, fontSize = 18.sp)
                    Text(text = "${state.contemporaryYearStart} - ${state.contemporaryYearEnd}", fontSize = 18.sp)
                    Text(text = "${state.historicalYearStart} - ${state.historicalYearEnd}", fontSize = 18.sp)
                    Text(text = "${state.monthStart} - ${state.monthEnd}", fontSize = 18.sp)
                    Text(text = "${state.points.size} points, ${"%,d".format(viewModel.polygonArea().toInt())} km²", fontSize = 18.sp)
                    Text(text = "${state.indices.toList()}", fontSize = 18.sp)
                }
            }
            Button(onClick = {
                viewModel.saveRoi(filesDir) { success ->
                    if (success) {
                        doneClick()
                    } else {
                        snackbar("Failed to save ROI")
                    }
                }
            }) {
                Text(text = "Done", fontSize = 18.sp)
            }
        }
    }

    @Composable
    fun Progress() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(152.dp))
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

        for (latlng in viewModel.state.value.points) {
            map.addMarker(MarkerOptions().position(latlng))?.let { marker ->
                markerAdd(marker)
            }
        }

        addPolygon(map)

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

    private fun addPolygon(map: GoogleMap) {
        viewModel.polygonOpts()?.let { opts ->
            polyGetter()?.remove()
            polySetter(map.addPolygon(opts))
        }
    }
}

object RoiRoutes {
    const val list = "roi"
    const val name = "roi_name"
    const val contemporaryYears = "roi_cont_dates"
    const val historicalYears = "roi_hist_dates"
    const val months = "roi_months"
    const val indices = "roi_indices"
    const val polygon = "roi_polygon"
    const val overview = "roi_overview"
}