package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.PolygonUtils
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygons

object PolygonsOption {
    interface Model: CollectPolygons.AppBarTitler {
        var polygonName: String

        val polygons: MutableList<PolygonUtils.NamedPolygon>

        fun loadDrawnPolygonsFile(callback: (Result<DrawnPolygonsFile>) -> Unit): Job
        @Composable
        fun OptionsInit(snack: SnackFun, back: Click, content: @Composable () -> Unit)
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, @StringRes polygonTypePlural: Int, maxPolygons: Int, back: Click, skip: Click, yes: Click, no: Click) {
        appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))
        model.polygonName = ""
        model.OptionsInit(snack, back) {
            Choice(model, snack, polygonTypePlural, maxPolygons, skip, yes, no)
        }
    }

    @Composable
    fun Choice(model: Model, snack: SnackFun, @StringRes polygonTypePlural: Int, maxPolygons: Int, skip: Click, yes: Click, no: Click) {
        val (drawnPolys, setDrawnPolys) = remember { mutableStateOf<Result<DrawnPolygonsFile>?>(null) }

        when {
            drawnPolys == null -> {
                Progress()
                model.loadDrawnPolygonsFile(setDrawnPolys)
            }
            drawnPolys.isFailure -> Layout(model, snack, polygonTypePlural, maxPolygons, yes, no)
            else -> {
                model.polygons.clear()
                model.polygons.addAll(drawnPolys.getOrNull()!!.polygons)
                skip()
            }
        }
    }

    @Composable
    fun Layout(model: Model, snack: SnackFun, @StringRes polygonTypePlural: Int, maxPolygons: Int, yes: Click, no: Click) {
        val notFirst by remember { mutableStateOf(model.polygons.isNotEmpty()) }

        var header = R.string.would_you_like_polygons
        var yesButton = R.string.yes_add_polygons
        var noButton = R.string.no_skip_polygons

        if (notFirst) {
            header = R.string.would_you_like_more_polygons
            yesButton = R.string.yes_add_more_polygons
            noButton = R.string.no_skip_more_polygons
        }

        val plural = stringResource(polygonTypePlural)
        val tooMany = stringResource(R.string.please_hit_no).format(maxPolygons.toString(), plural)
        Col.Dash(stringResource(header).format(plural)) {
            DashboardButton(stringResource(yesButton).format(plural)) {
                if (model.polygons.size < maxPolygons) {
                    yes()
                } else {
                    snack(tooMany)
                }
            }
            DashboardButton(stringResource(noButton).format(plural)) {
                no()
            }
        }
    }
}