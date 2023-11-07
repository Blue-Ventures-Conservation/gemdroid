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
        val title: Int
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

        b.composable(addPrefix(Routes.polygons_option)) {
            PolygonsOption.Screen(model, appBar, snack, back = {
                model.polygons.clear()
                model.drawer.points.clear()
                nav.popBackStack()
            }, skip = {
                nav.navigate(nextRoute) {
                    popUpTo(prevRoute)
                }
            }, yes = {
                var route = Routes.polygon_draw_or_shapefile
                if (model.named) {
                    route = Routes.polygon_name
                }
                nav.navigate(addPrefix(route))
            }, no = {
                var route = nextRoute
                if (model.polygons.size > 0) {
                    route = addPrefix(Routes.polygons_overview)
                }

                nav.navigate(route)
            })
        }

        b.composable(addPrefix(Routes.polygon_name)) {
            NamePolygon.Screen(model, appBar, snack, nav::popBackStack) {
                nav.navigate(addPrefix(Routes.polygon_draw_or_shapefile))
            }
        }

        b.composable(addPrefix(Routes.polygon_draw_or_shapefile)) {
            DrawOrShapefile.Screen(model, appBar, nav::popBackStack, draw = {
                nav.navigate(addPrefix(Routes.drawn_polygon))
            }) {
                nav.navigate(addPrefix(Routes.shapefile_polygon))
            }
        }

        b.composable(addPrefix(Routes.drawn_polygon)) {
            DrawPolygon.Screen(model, appBar, snack, {
                model.drawer.points.clear()
                nav.popBackStack()
            }) {
                nav.popBackStack(addPrefix(Routes.polygons_option), false)
            }
        }

        b.composable(addPrefix(Routes.shapefile_polygon)) {
            ShapefilePolygon.Screen(model, appBar, snack, nav::popBackStack) {
                nav.navigate(addPrefix(Routes.visualize_shapefile_polygon))
            }
        }

        b.composable(addPrefix(Routes.visualize_shapefile_polygon)) {
            VisualizeShapefile.Screen(model, appBar, { nav.popBackStack(addPrefix(Routes.polygon_draw_or_shapefile), true) }) {
                nav.popBackStack(addPrefix(Routes.polygons_option), false)
            }
        }

        b.composable(addPrefix(Routes.polygons_overview)) {
            PolygonsOverview.Screen(model, appBar, {
                model.polygons.clear()
                model.drawer.points.clear()
                nav.popBackStack(addPrefix(Routes.polygons_option), false)
            }) {
                nav.navigate(nextRoute)
            }
        }
    }
}