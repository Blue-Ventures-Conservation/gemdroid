package org.blueventures.gemdroid.ui.common.polygons

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawOrShapefile
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawPolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.NamePolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.PolygonsOption
import org.blueventures.gemdroid.ui.common.polygons.screens.PolygonsOverview
import org.blueventures.gemdroid.ui.common.polygons.screens.ShapefilePolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.VisualizeShapefile

object Polygons {
    interface Model: AppBarTitler, PolygonsOption.Model, NamePolygon.Model, DrawOrShapefile.Model, DrawPolygon.Model, ShapefilePolygon.Model, VisualizeShapefile.Model, PolygonsOverview.Model {
        val named: Boolean
    }

    interface AppBarTitler {
        val appBarTitleId: Int
        fun appBarTitle(title: String): String
    }

    object Routes {
        const val polygons_option = "polygons_option"
        const val polygon_name = "polygon_name"
        const val polygon_draw_or_shapefile = "polygon_draw_or_shapefile"
        const val drawn_polygon = "drawn_polygon"
        const val shapefile_polygon = "shapefile_polygon"
        const val visualize_shapefile_polygon = "visualize_shapefile_polygon"
        const val polygons_overview = "polygons_overview"
    }

    fun screens(
        b: NavGraphBuilder,
        nav: NavHostController,
        routePrefix: String,
        prevRoute: String,
        nextRoute: String,
        appBar: AppBar,
        snack: SnackFun,
        model: Model,
    ) {
        val addPrefix: (String) -> String = { routePrefix + it }
        val routePolygonsOption = addPrefix(Routes.polygons_option)
        val routePolygonName = addPrefix(Routes.polygon_name)
        val routePolygonDrawOrShp = addPrefix(Routes.polygon_draw_or_shapefile)
        val routeDrawnPolygon = addPrefix(Routes.drawn_polygon)
        val routeShpPolygon = addPrefix(Routes.shapefile_polygon)
        val routeVisualizeShp = addPrefix(Routes.visualize_shapefile_polygon)
        val routePolygonsOverview = addPrefix(Routes.polygons_overview)

        b.composable(routePolygonsOption) {
            PolygonsOption.Screen(model, appBar, snack, back = {
                model.polygons.clear()
                model.drawer.points.clear()
                nav.popBackStack()
            }, skip = {
                nav.navigate(nextRoute) {
                    popUpTo(prevRoute)
                }
            }, yes = {
                var route = routePolygonDrawOrShp
                if (model.named) {
                    route = routePolygonName
                }
                nav.navigate(route)
            }, no = {
                var route = nextRoute
                if (model.polygons.size > 0) {
                    route = routePolygonsOverview
                }

                nav.navigate(route)
            })
        }

        b.composable(routePolygonName) {
            NamePolygon.Screen(model, appBar, snack, nav::popBackStack) {
                nav.navigate(routePolygonDrawOrShp)
            }
        }

        b.composable(routePolygonDrawOrShp) {
            DrawOrShapefile.Screen(model, appBar, nav::popBackStack, draw = {
                nav.navigate(routeDrawnPolygon)
            }) {
                nav.navigate(routeShpPolygon)
            }
        }

        b.composable(routeDrawnPolygon) {
            DrawPolygon.Screen(model, appBar, snack, {
                model.drawer.points.clear()
                nav.popBackStack()
            }) {
                nav.popBackStack(routePolygonsOption, false)
            }
        }

        b.composable(routeShpPolygon) {
            ShapefilePolygon.Screen(model, appBar, snack, nav::popBackStack) {
                nav.navigate(routeVisualizeShp)
            }
        }

        b.composable(routeVisualizeShp) {
            VisualizeShapefile.Screen(model, appBar, { nav.popBackStack(routePolygonDrawOrShp, true) }) {
                nav.popBackStack(routePolygonsOption, false)
            }
        }

        b.composable(routePolygonsOverview) {
            PolygonsOverview.Screen(model, appBar, {
                model.polygons.clear()
                model.drawer.points.clear()
                nav.popBackStack(routePolygonsOption, false)
            }) {
                nav.navigate(nextRoute)
            }
        }
    }
}