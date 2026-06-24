package org.blueventures.gemdroid.ui.common.polygons

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawOrUpload
import org.blueventures.gemdroid.ui.common.polygons.screens.DrawPolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.NamePolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.PolygonsOption
import org.blueventures.gemdroid.ui.common.polygons.screens.FilePolygon
import org.blueventures.gemdroid.ui.common.polygons.screens.VisualizeFilePoly

object CollectPolygons {
    interface Model: AppBarTitler, PolygonsOption.Model, NamePolygon.Model, DrawOrUpload.Model, DrawPolygon.Model, FilePolygon.Model, VisualizeFilePoly.Model {
        val named: Boolean
        fun goBack()
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

        b.backHandler(routePolygonsOption, {
            model.polygons.clear()
            model.drawer.clear()
            model.goBack()
            nav.popBackStack()
        }) { back ->
            PolygonsOption.Screen(model, appBar, snack, back, skip = {
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
                nav.navigate(nextRoute)
            })
        }

        b.backHandler(routePolygonName, {
            model.polygonName = ""
            nav.popBackStack()
        }) {
            NamePolygon.Screen(model, appBar, snack) {
                nav.navigate(routePolygonDrawOrShp)
            }
        }

        b.backHandler(routePolygonDrawOrShp, nav::popBackStack) {
            DrawOrUpload.Screen(model, appBar, draw = {
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
                nav.popBackStack(routePolygonsOption, false)
            }
        }

        b.backHandler(routeShpPolygon, nav::popBackStack) {
            FilePolygon.Screen(model, appBar, snack) {
                nav.navigate(routeVisualizeShp)
            }
        }

        b.backHandler(routeVisualizeShp, {
            nav.popBackStack(routePolygonDrawOrShp, false)
        }) {
            VisualizeFilePoly.Screen(model, appBar) {
                nav.popBackStack(routePolygonsOption, false)
            }
        }
    }
}