package org.blueventures.gemdroid.ui.roi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
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
import java.io.File

object Roi {
    @Composable
    fun List(filesDir : File, viewModel: RoiViewModel, roiClick: (File) -> Unit, floatingOnClick: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(onClick = floatingOnClick, modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd)) {
                Icon(Icons.Filled.Add, "")
            }

            ListView(filesDir, viewModel)
        }
    }

    @Composable
    fun ListView(filesDir: File, viewModel: RoiViewModel) {
        val state by viewModel.state.collectAsState()
        if (state.rois == null) {
            Progress()

            viewModel.refreshRois(filesDir)
        } else {
            state.rois?.let { rois ->
                if (rois.isEmpty()) {
                    Text(
                        text = "No Regions of Interest (ROIs) yet, create one by tapping the plus button!",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                } else {

                }
            }
        }
    }

    @Composable
    fun Item() {

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
            Spacer(modifier = Modifier.height(0.dp))
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
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
            onValueChange = {
                viewModel.setMonthEnd(it)
                month = it
            }
        )
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
    fun Overview(viewModel: RoiViewModel, doneClick: () -> Unit) {
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
                }
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = state.name, fontSize = 18.sp)
                    Text(text = "${state.contemporaryYearStart} - ${state.contemporaryYearEnd}", fontSize = 18.sp)
                    Text(text = "${state.historicalYearStart} - ${state.historicalYearEnd}", fontSize = 18.sp)
                    Text(text = "${state.monthStart} - ${state.monthEnd}", fontSize = 18.sp)
                    Text(text = "${state.points.size} points, ${"%,d".format(viewModel.polygonArea().toInt())} km²", fontSize = 18.sp)
                }
            }
            Button(onClick = {
                doneClick()
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
    const val contemporayYears = "roi_cont_dates"
    const val historicalYears = "roi_hist_dates"
    const val months = "roi_months"
    const val polygon = "roi_polygon"
    const val overview = "roi_overview"
}

object RoiView {
    const val roiArg = "roi"
    const val routeWithArgs = "roi/{$roiArg}"
}