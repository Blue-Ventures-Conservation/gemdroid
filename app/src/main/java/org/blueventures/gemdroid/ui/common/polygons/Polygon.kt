package org.blueventures.gemdroid.ui.common.polygons

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
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

        b.backHandler(routePolygonDrawOrShp, nav::popBackStack) {
            DrawOrShapefile.Screen(model, appBar, draw = {
                nav.navigate(routeDrawnPolygon)
            }) {
                nav.navigate(routeShpPolygon)
            }
        }

        b.backHandler(routeDrawnPolygon, {
            model.drawer.clear()
            nav.popBackStack()
        }) {
            DrawPolygon.Screen(model, appBar, snack) {
                nav.navigate(nextRoute)
            }
        }

        b.backHandler(routeShpPolygon, nav::popBackStack) {
            ShapefilePolygon.Screen(model, appBar, snack) {
                nav.navigate(routeVisualizeShp)
            }
        }

        b.backHandler(routeVisualizeShp, {
            model.drawer.clear()
            nav.popBackStack(routePolygonDrawOrShp, false)
        }) {
            VisualizeShapefile.Screen(model, appBar) {
                nav.navigate(nextRoute)
            }
        }
    }
}