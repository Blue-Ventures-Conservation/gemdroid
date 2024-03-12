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
        const val option = "polygons_option"
        const val name = "polygons_polygon_name"
        const val draw_or_shapefile = "polygons_draw_or_shapefile"
        const val drawn = "polygons_drawn"
        const val shapefile = "polygons_shapefile"
        const val visualize_shapefile = "polygons_visualize_shapefile"
        const val overview = "polygons_overview"
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
        val routePolygonsOption = addPrefix(Routes.option)
        val routePolygonName = addPrefix(Routes.name)
        val routePolygonDrawOrShp = addPrefix(Routes.draw_or_shapefile)
        val routeDrawnPolygon = addPrefix(Routes.drawn)
        val routeShpPolygon = addPrefix(Routes.shapefile)
        val routeVisualizeShp = addPrefix(Routes.visualize_shapefile)
        val routePolygonsOverview = addPrefix(Routes.overview)

        b.composable(routePolygonsOption) {
            PolygonsOption.Screen(model, appBar, snack, back = {
                model.polygons.clear()
                model.drawer.clear()
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
                model.drawer.clear()
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
            VisualizeShapefile.Screen(model, appBar, { nav.popBackStack(routePolygonDrawOrShp, false) }) {
                nav.popBackStack(routePolygonsOption, false)
            }
        }

        b.composable(routePolygonsOverview) {
            PolygonsOverview.Screen(model, appBar, {
                model.polygons.clear()
                model.drawer.clear()
                nav.popBackStack(routePolygonsOption, false)
            }) {
                nav.navigate(nextRoute)
            }
        }
    }
}