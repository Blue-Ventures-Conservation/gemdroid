package org.blueventures.gemdroid.ui.common.polygons

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawOrShapefile
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawPolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.ShapefilePolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.VisualizeShapefile

object Polygon {
    interface Model: Polygons.AppBarTitler, DrawOrShapefile.Model, DrawPolygon.Model, ShapefilePolygon.Model, VisualizeShapefile.Model

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
        model: Model,
    ) {
        val addPrefix: (String) -> String = { routePrefix + it }
        val routePolygonDrawOrShp = addPrefix(Routes.draw_or_shapefile)
        val routeDrawnPolygon = addPrefix(Routes.drawn)
        val routeShpPolygon = addPrefix(Routes.shapefile)
        val routeVisualizeShp = addPrefix(Routes.visualize_shapefile)

        b.composable(routePolygonDrawOrShp) {
            DrawOrShapefile.Screen(model, appBar, nav::popBackStack, draw = {
                nav.navigate(routeDrawnPolygon)
            }) {
                nav.navigate(routeShpPolygon)
            }
        }

        b.composable(routeDrawnPolygon) {
            DrawPolygon.Screen(model, appBar, snack, {
                model.drawer.clear()
                nav.popBackStack()
            }) {
                nav.navigate(nextRoute)
            }
        }

        b.composable(routeShpPolygon) {
            ShapefilePolygon.Screen(model, appBar, snack, nav::popBackStack) {
                nav.navigate(routeVisualizeShp)
            }
        }

        b.composable(routeVisualizeShp) {
            VisualizeShapefile.Screen(model, appBar, {
                model.drawer.clear()
                nav.popBackStack(routePolygonDrawOrShp, false)
            }) {
                nav.navigate(nextRoute)
            }
        }
    }
}