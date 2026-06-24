package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.localized
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.FileStream
import org.blueventures.gemdroid.data.MultiPolyPts
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.PolygonFile
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygons

object FilePolygon {
    interface Model: CollectPolygons.AppBarTitler {
        var filePoly: MultiPolyPts
        fun <T> background(work: () -> T, callback: (T) -> Unit): Job
        fun validatePolygonFile(streams: FileStream.Streams, callback: (Result<MultiPolyPts>) -> Unit): Job
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, next: Click) {
        appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))

        val context = LocalContext.current
        Col.Col {
            PolygonFile.Result(stringResource(R.string.upload_a_poly_file), { uris, callback ->
                FileStream.makeStreams(context, model::background, uris) { streams ->
                    model.validatePolygonFile(streams, callback)
                }
            }, { result ->
                when {
                    result.isSuccess -> {
                        model.filePoly = result.getOrNull()!!
                        next()
                    }
                    else -> snack(result.exceptionOrNull()!!.localized(context))
                }
            }, {})
        }
    }
}