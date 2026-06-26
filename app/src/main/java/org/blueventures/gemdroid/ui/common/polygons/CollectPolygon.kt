package org.blueventures.gemdroid.ui.common.polygons

import androidx.annotation.StringRes
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawOrUpload
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawPolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.FilePolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.VisualizeFilePoly

object CollectPolygon {
    interface Model: CollectPolygons.AppBarTitler, DrawOrUpload.Model, DrawPolygon.Model, FilePolygon.Model, VisualizeFilePoly.Model

    object Routes {
        const val draw_or_shapefile = "polygon_draw_or_shapefile"
        const val drawn = "polygon_drawn"
        const val shapefile = "polygon_shapefile"
        const val visualize_shapefile = "polygon_visualize_shapefile"
    }

    fun screens(
        b: NavGraphBuilder,
        nav: NavHostController,
        routePrefix: String,
        nextRoute: String,
        appBar: AppBar,
        snack: SnackFun,
        storage: Maps.Storage?,
        @StringRes polygonType: Int,
        attemptGps: Boolean,
        shpColor: Int?,
        model: Model,
    ) {
        val addPrefix: (String) -> String = { routePrefix + it }
        val routePolygonDrawOrShp = addPrefix(Routes.draw_or_shapefile)
        val routeDrawnPolygon = addPrefix(Routes.drawn)
        val routeShpPolygon = addPrefix(Routes.shapefile)
        val routeVisualizeShp = addPrefix(Routes.visualize_shapefile)

        b.backHandler(routePolygonDrawOrShp, nav::popBackStack) {
            DrawOrUpload.Screen(model, appBar, polygonType, draw = {
                nav.navigate(routeDrawnPolygon)
            }) {
                nav.navigate(routeShpPolygon)
            }
        }

        b.backHandler(routeDrawnPolygon, {
            model.drawnPoints = emptyList()
            nav.popBackStack()
        }) {
            DrawPolygon.Screen(model, appBar, snack, storage, polygonType, attemptGps) {
                nav.navigate(nextRoute)
            }
        }

        b.backHandler(routeShpPolygon, nav::popBackStack) {
            FilePolygon.Screen(model, appBar, snack) {
                nav.navigate(routeVisualizeShp)
            }
        }

        b.backHandler(routeVisualizeShp, {
            model.drawnPoints = emptyList()
            nav.popBackStack(routePolygonDrawOrShp, false)
        }) {
            VisualizeFilePoly.Screen(model, appBar, storage, polygonType, shpColor) {
                nav.navigate(nextRoute)
            }
        }
    }
}