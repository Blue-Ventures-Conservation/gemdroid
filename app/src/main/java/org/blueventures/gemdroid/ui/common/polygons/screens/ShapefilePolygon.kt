package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Shapefile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object ShapefilePolygon {
    interface Model: Polygons.AppBarTitler {
        var shapefile: MultiPolyPts
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
        fun validateShapefile(streams: Shapefile.Streams, callback: (Result<MultiPolyPts>) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, next: Click) {
        appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))
        Col.Col {
            Shapefile.Screen(stringResource(R.string.upload_a_shapefile), model::background, model::validateShapefile, { err ->
                snack(err)
            }) { points ->
                model.shapefile = points
                next()
            }
        }
    }
}