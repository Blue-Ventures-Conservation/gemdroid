package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.DrawnPolygonsFile
import org.blueventures.gemdroid.data.PolygonDrawer
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object PolygonsOption {
    interface Model: Polygons.AppBarTitler {
        var polygonName: String

        val polygonType: Int
        val polygonTypePlural: Int
        val maxPolygons: Int
        val polygons: MutableList<PolygonDrawer.NamedPolygon>

        fun loadDrawnPolygonsFile(callback: (Result<DrawnPolygonsFile>) -> Unit): Job
        @Composable
        fun optionsInit(snack: SnackFun, back: Click, content: @Composable () -> Unit)
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, back: Click, skip: Click, yes: Click, no: Click) {
        appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))
        model.polygonName = ""
        model.optionsInit(snack, back) {
            Choice(model, snack, skip, yes, no)
        }
    }

    @Composable
    fun Choice(model: Model, snack: SnackFun, skip: Click, yes: Click, no: Click) {
        val (drawnPolys, setDrawnPolys) = remember { mutableStateOf<Result<DrawnPolygonsFile>?>(null) }

        when {
            drawnPolys == null -> {
                Progress()
                model.loadDrawnPolygonsFile(setDrawnPolys)
            }
            drawnPolys.isFailure -> Layout(model, snack, yes, no)
            else -> {
                model.polygons.clear()
                model.polygons.addAll(drawnPolys.getOrNull()!!.polygons)
                skip()
            }
        }
    }

    @Composable
    fun Layout(model: Model, snack: SnackFun, yes: Click, no: Click) {
        val notFirst by remember { mutableStateOf(model.polygons.isNotEmpty()) }

        var header = R.string.would_you_like_polygons
        var yesButton = R.string.yes_add_polygons
        var noButton = R.string.no_skip_polygons

        if (notFirst) {
            header = R.string.would_you_like_more_polygons
            yesButton = R.string.yes_add_more_polygons
            noButton = R.string.no_skip_more_polygons
        }

        val plural = stringResource(model.polygonTypePlural)
        val tooMany = stringResource(R.string.please_hit_no).format(model.maxPolygons.toString(), plural)
        Col.Dash(stringResource(header).format(plural)) {
            DashboardButton(stringResource(yesButton).format(plural)) {
                if (model.polygons.size < model.maxPolygons) {
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