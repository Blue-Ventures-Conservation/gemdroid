package org.blueventures.gemdroid.ui.common.polygons.screens

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.maps.Polygons
import org.blueventures.gemdroid.ui.common.maps.Visualize

object VisualizeFilePoly {
    interface Model {
        var polygonName: String
        var filePoly: MultiPolyPts

        fun visualizer(): Visualize.Visualizer?
        fun shapefileLooksGood()
        fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit)
        fun <T> background(work: () -> T, callback: (T) -> Unit)
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, storage: Maps.Storage?, @StringRes polygonTypePlural: Int, shpColor: Int?, next: Click) {
        val (center, setCenter) = remember { mutableStateOf<LatLng?>(null) }
        if (center == null) {
            model.background({
                PolygonUtils.centerFromMultiPoly(model.filePoly)
            }) { cent ->
                setCenter(cent)
            }
        } else {
            val title = stringResource(R.string.visualize_polygon)
            Visualize.Screen(model.visualizer(), appBar, title, false, center = center, storage = storage, poly = object : Polygons.Model() {
                override val touchEnabled = true
                override fun onTouch(point: Polygons.NamedPoint?) = @Composable { Polygons.PlaceMarker(point) }

                override fun polygonGroups(context: Context, callback: (List<Polygons.Group>) -> Unit) {
                    return model.polygonGroups(context) { groups ->
                        val list = mutableListOf<Polygons.Group>()

                        if (model.filePoly.isNotEmpty()) {
                            val newGroupTitle = context.getString(polygonTypePlural)
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
                                callback(mutableListOf(Polygons.Group(newGroupTitle, listOf(newGroupPoly), shpColor)).apply {
                                    addAll(groups)
                                })
                            }
                        } else {
                            callback(groups)
                        }
                    }
                }
            }, next = Maps.FloatingNext(Icons.Filled.Check, R.string.polygon_looks_good) {
                model.shapefileLooksGood()
                next()
            })
        }
    }
}