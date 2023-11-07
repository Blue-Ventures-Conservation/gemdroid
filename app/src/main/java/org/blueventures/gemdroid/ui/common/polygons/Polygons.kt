package org.blueventures.gemdroid.ui.common.polygons

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
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
        var skipped: Boolean
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
        model: Model,
        appBar: AppBar,
        snack: SnackFun
    ): Click {
        val addPrefix: (String) -> String = { routePrefix + it }

        b.composable(addPrefix(Routes.polygons_option)) {
            PolygonsOption.Screen(model, appBar, snack, back = {
                model.polygons.clear()
                nav.popClear(prevRoute)
            }, skip = {
                model.skipped = true
                nav.popClear(nextRoute)
            }, yes = {
                nav.navigate(addPrefix(Routes.polygon_name))
            }, no = {
                var route = nextRoute
                if (model.polygons.size > 1) {
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
                nav.popClear(addPrefix(Routes.polygons_option))
            }
        }

        b.composable(addPrefix(Routes.shapefile_polygon)) {
            ShapefilePolygon.Screen(model, appBar, snack, nav::popBackStack) {
                nav.navigate(addPrefix(Routes.visualize_shapefile_polygon))
            }
        }

        b.composable(addPrefix(Routes.visualize_shapefile_polygon)) {
            VisualizeShapefile.Screen(model, appBar, { nav.popClear(addPrefix(Routes.polygon_draw_or_shapefile)) }) {
                nav.popClear(addPrefix(Routes.polygons_option))
            }
        }

        b.composable(addPrefix(Routes.polygons_overview)) {
            model.drawer.points.clear()
            PolygonsOverview.Screen(model, appBar, nav::popBackStack, {
                nav.navigate(nextRoute)
            }) {
                model.polygons.clear()
                nav.popClear(addPrefix(Routes.polygons_option))
            }
        }

        return {
            if (model.skipped) {
                model.skipped = false
                model.polygons.clear()
                nav.popClear(prevRoute)
            } else {
                nav.popBackStack()
            }
        }
    }
}