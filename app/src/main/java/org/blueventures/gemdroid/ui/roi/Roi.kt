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
import org.blueventures.gemdroid.ui.common.polygons.Polygons
import org.blueventures.gemdroid.ui.roi.screens.CoarseRoi
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
        const val polygon = prefix + "polygon"
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
        b.composable(Routes.name) {
            Name.Screen(viewModel, appBar, snack, back = {
                viewModel.clear()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.contemporaryYears)
            }
        }

        // ROI contemporary years selection
        b.composable(Routes.contemporaryYears) {
            ContemporaryYears.Screen(viewModel, appBar, snack, back = {
                viewModel.clearContemporaryYears()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.contemporaryMonths)
            }
        }

        // ROI contemporary months selection
        b.composable(Routes.contemporaryMonths) {
            ContemporaryMonths.Screen(viewModel, appBar, back = {
                viewModel.clearContemporaryMonths()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.historicalYears)
            }
        }

        // ROI historical years selection
        b.composable(Routes.historicalYears) {
            HistoricalYears.Screen(viewModel, appBar, snack, back = {
                viewModel.clearHistoricalYears()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.historicalMonths)
            }
        }

        // ROI months range selection
        b.composable(Routes.historicalMonths) {
            HistoricalMonths.Screen(viewModel, appBar, back = {
                viewModel.clearHistoricalMonths()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.polygon)
            }
        }

        // ROI polygon creation
        b.composable(Routes.polygon) {
            CoarseRoi.Screen(viewModel, appBar, snack, back = {
                viewModel.roiDrawer.points.clear()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.prefix + Polygons.Routes.polygons_option)
            }
        }

        Polygons.screens(b, nav, Routes.prefix, Routes.polygon, Routes.overview, appBar, snack, viewModel)

        // ROI overview
        b.composable(Routes.overview) {
            Overview.Screen(viewModel, activity.filesDir, appBar, snack, nav::popBackStack) {
                viewModel.clear()
                nav.popClear(Routes.list)
            }
        }
    }
}