package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.ui.analysis.cra.screens.Common
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.PolygonFile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object FilePolygon {
    interface Model: Polygons.AppBarTitler {
        var filePoly: MultiPolyPts
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
        fun validatePolygonFile(streams: PolygonFile.Streams, callback: (Result<MultiPolyPts>) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, next: Click) {
        appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))

        val context = LocalContext.current
        Col.Col {
            PolygonFile.Screen(stringResource(R.string.upload_a_poly_file), { uris, callback ->
                Common.makeStreams(context, model::background, uris) { streams ->
                    model.validatePolygonFile(streams, callback)
                }
            }, { err ->
                snack(err)
            }) { points ->
                model.filePoly = points
                next()
            }
        }
    }
}