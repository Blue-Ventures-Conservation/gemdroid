package org.blueventures.gemdroid.ui.roi

import android.app.Activity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygon
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.roi.screens.CoarsePolygonPurpose
import org.blueventures.gemdroid.ui.roi.screens.ContemporaryMonths
import org.blueventures.gemdroid.ui.roi.screens.ContemporaryYears
import org.blueventures.gemdroid.ui.roi.screens.HistoricalMonths
import org.blueventures.gemdroid.ui.roi.screens.HistoricalYears
import org.blueventures.gemdroid.ui.roi.screens.Name
import org.blueventures.gemdroid.ui.roi.screens.Overview
import org.blueventures.gemdroid.ui.roi.screens.RoiList
import java.io.File

object Roi {
    object Routes {
        const val prefix = "roi_"
        const val list = prefix + "roi"
        const val name = prefix + "name"
        const val contemporaryYears = prefix + "cont_dates"
        const val contemporaryMonths = prefix + "cont_months"
        const val historicalYears = prefix + "hist_dates"
        const val historicalMonths = prefix + "hist_months"
        const val coarse_polygon_purpose = prefix + "coarse_polygon_purpose"
        const val overview = prefix + "overview"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, activity: Activity, viewModel: RoiViewModel, analysisModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun) {
        // ROI list
        b.composable(Routes.list) {
            RoiList.Screen(viewModel, object : RoiList.DirHolder {
                override var roiDir = File("")
                    set(value) { field = value; analysisModel.roiDir = value }
            }, activity.filesDir, appBar, snack, next = {
                nav.navigate(Analysis.Routes.dashboard)
            }) {
                nav.navigate(Routes.name)
            }
        }

        // ROI name creation
        b.backHandler(Routes.name, {
            viewModel.clearState()
            nav.popBackStack()
        }) {
            Name.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.contemporaryYears)
            }
        }

        // ROI contemporary years selection
        b.backHandler(Routes.contemporaryYears, {
            viewModel.clearContemporaryYears()
            nav.popBackStack()
        }) {
            ContemporaryYears.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.contemporaryMonths)
            }
        }

        // ROI contemporary months selection
        b.backHandler(Routes.contemporaryMonths, {
            viewModel.clearContemporaryMonths()
            nav.popBackStack()
        }) {
            ContemporaryMonths.Screen(viewModel, appBar) {
                nav.navigate(Routes.historicalYears)
            }
        }

        // ROI historical years selection
        b.backHandler(Routes.historicalYears, {
            viewModel.clearHistoricalYears()
            nav.popBackStack()
        }) {
            HistoricalYears.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.historicalMonths)
            }
        }

        // ROI months range selection
        b.backHandler(Routes.historicalMonths, {
            viewModel.clearHistoricalMonths()
            nav.popBackStack()
        }) {
            HistoricalMonths.Screen(viewModel, appBar) {
                if (viewModel.importedROI.isEmpty()) {
                    nav.navigate(Routes.coarse_polygon_purpose)
                } else {
                    nav.navigate(Routes.prefix+Polygons.Routes.option)
                }
            }
        }

        b.backHandler(Routes.coarse_polygon_purpose, nav::popBackStack) {
            CoarsePolygonPurpose.Screen(appBar) {
                nav.navigate(Routes.prefix+Polygon.Routes.draw_or_shapefile) {
                    popUpTo(Routes.historicalMonths)
                }
            }
        }

        // Coarse ROI boundary creation
        Polygon.screens(b, nav, Routes.prefix, Routes.prefix+Polygons.Routes.option, appBar, snack, viewModel.coarseModel)

        // Sub-Regions
        Polygons.screens(b, nav, Routes.prefix, Routes.prefix+Polygon.Routes.draw_or_shapefile, Routes.overview, appBar, snack, viewModel)

        // ROI overview
        b.backHandler(Routes.overview, nav::popBackStack) {
            Overview.Screen(viewModel, activity.filesDir, appBar, snack) {
                viewModel.clearState()
                nav.popClear(Routes.list)
            }
        }
    }
}