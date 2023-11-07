package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object ShapefilePolygon {
    interface Model: Polygons.AppBarTitler {
        var shapefile: List<List<LatLng>>
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
        fun validateShapefile(streams: Shapefile.Streams, callback: (Result<List<List<LatLng>>>?) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.title))))
            Col.Col {
                Shapefile.Screen(stringResource(R.string.select_a_shapefile), { bg ->
                    model.background(bg) {}
                }, model::validateShapefile, { err ->
                    snack(err)
                }) { points ->
                    model.shapefile = points
                    next()
                }
            }
        }
    }
}