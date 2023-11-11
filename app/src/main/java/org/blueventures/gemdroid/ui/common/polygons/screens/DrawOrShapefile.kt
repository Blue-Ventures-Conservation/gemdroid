package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Col.DashboardButton
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object DrawOrShapefile {
    interface Model: Polygons.AppBarTitler {
        val polygonType: Int
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, back: Click, draw: Click, shapefile: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.appBarTitleId))))
            val polyType = stringResource(model.polygonType)
            Col.Dash(stringResource(R.string.draw_polygon_or_shapefile).format(polyType)) {
                DashboardButton(stringResource(R.string.draw_a_polygon).format(polyType), draw)
                DashboardButton(stringResource(R.string.use_a_shapefile), shapefile)
            }
        }
    }
}