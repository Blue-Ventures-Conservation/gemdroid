package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygons

object DrawOrUpload {
    interface Model: CollectPolygons.AppBarTitler {
        fun edit(): Boolean
        fun clearEdit()
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, @StringRes polygonType: Int, draw: Click, shapefile: Click) {
        appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))
        val polyType = stringResource(polygonType)

        val edit by remember { mutableStateOf(model.edit()) }

        var header = stringResource(R.string.draw_polygon_or_upload).format(polyType)
        var drawButton = stringResource(R.string.draw_a_polygon).format(polyType)
        var shapefileButton = stringResource(R.string.upload_a_poly_file)
        if (edit) {
            header = stringResource(R.string.edit_polygon_or_replace).format(polyType)
            drawButton = stringResource(R.string.edit_a_polygon).format(polyType)
            shapefileButton = stringResource(R.string.replace_with_a_polygon_file)
        }

        model.clearEdit()

        Col.Dash(header) {
            DashboardButton(drawButton, draw)
            DashboardButton(shapefileButton, shapefile)
        }
    }
}