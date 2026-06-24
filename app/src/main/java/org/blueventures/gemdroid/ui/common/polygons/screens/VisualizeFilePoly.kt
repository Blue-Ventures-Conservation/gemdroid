package org.blueventures.gemdroid.ui.common.polygons.screens

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Bounds
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Maps.MapActionButton
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.maps.Visualize

object VisualizeFilePoly {
    interface Model {
        var polygonName: String
        var visualizer: Visualize.Visualizer?
        var filePoly: MultiPolyPts

        val polygonTypePlural: Int
        val storage: Maps.Storage?
        val shpColor: Int?

        fun shapefileLooksGood()
        fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit): Job
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, next: Click) {
        val (gotCenter, setGotCenter) = remember { mutableStateOf(false) }
        val (center, setCenter) = remember { mutableStateOf<LatLng?>(null) }
        if (!gotCenter) {
            model.background({
                Bounds.centerFromMultiPoly(model.filePoly)
            }) { cent ->
                setGotCenter(true)
                setCenter(cent)
            }
        } else {
            val title = stringResource(R.string.visualize_polygon)
            Visualize.Screen(model.visualizer, appBar, title, false, center = center, storage = model.storage, poly = object : Polygons.Model() {
                override val touchEnabled = true
                override fun markerWork(work: () -> MarkerOptions?, callback: (MarkerOptions?) -> Unit) = model.background(work, callback)

                override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit): Job {
                    return model.polygonGroups(context) { groups ->
                        val list = mutableListOf<Polygons.Group>()

                        if (model.filePoly.isNotEmpty()) {
                            val newGroupTitle = context.getString(model.polygonTypePlural)
                            val newGroupPoly = Polygons.Named(model.polygonName, model.filePoly)

                            var match = false
                            for (group in groups) {
                                if (group.menuTitle == newGroupTitle) {
                                    match = true
                                    list.add(Polygons.Group(group.menuTitle, mutableListOf(newGroupPoly).apply {
                                        addAll(group.polygons)
                                    }, group.color))
                                } else {
                                    list.add(group)
                                }
                            }

                            if (match) {
                                callback(list)
                            } else {
                                callback(mutableListOf(Polygons.Group(newGroupTitle, listOf(newGroupPoly), model.shpColor)).apply {
                                    addAll(groups)
                                })
                            }
                        } else {
                            callback(groups)
                        }
                    }
                }
            }) {
                MapActionButton({
                    model.shapefileLooksGood()
                    next()
                }) { Icon(Icons.Filled.Check, stringResource(R.string.polygon_looks_good)) }
            }
        }
    }
}