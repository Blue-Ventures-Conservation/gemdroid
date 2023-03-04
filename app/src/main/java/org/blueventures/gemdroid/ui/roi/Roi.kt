package org.blueventures.gemdroid.ui.roi

import android.app.Activity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.SnackFun

object Roi {
    object Routes {
        const val list = "roi"
        const val name = "roi_name"
        const val contemporaryYears = "roi_cont_dates"
        const val historicalYears = "roi_hist_dates"
        const val months = "roi_months"
        const val indices = "roi_indices"
        const val polygon = "roi_polygon"
        const val overview = "roi_overview"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, activity: Activity, roiModel: RoiViewModel, analysisModel: AnalysisViewModel, appBar: AppBarFun, snack: SnackFun) {
        // ROI list
        b.composable(Routes.list) {
            RoiList.Screen(roiModel, activity.filesDir, appBar, snack, roiClick = { dir ->
                analysisModel.clear()
                analysisModel.roiDir = dir
                nav.popClear(Analysis.Routes.dashboard)
            }) {
                nav.popClear(Routes.name)
            }
        }

        // ROI name creation
        b.composable(Routes.name) {
            Name.Screen(roiModel, appBar, snack, back = {
                roiModel.clearName()
                nav.popClear(Routes.list)
            }) {
                nav.navigate(Routes.contemporaryYears)
            }
        }

        // ROI contemporary years selection
        b.composable(Routes.contemporaryYears) {
            ContemporaryYears.Screen(roiModel, snack, back = {
                roiModel.clearContemporaryYears()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.historicalYears)
            }
        }

        // ROI historical years selection
        b.composable(Routes.historicalYears) {
            HistoricalYears.Screen(roiModel, snack, back = {
                roiModel.clearHistoricalYears()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.months)
            }
        }

        // ROI months range selection
        b.composable(Routes.months) {
            Months.Screen(roiModel, snack, back = {
                roiModel.clearMonths()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.indices)
            }
        }

        // ROI indices selection
        b.composable(Routes.indices) {
            Indices.Screen(roiModel, back = {
                roiModel.clearIndices()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.polygon)
            }
        }

        // ROI polygon creation
        b.composable(Routes.polygon) {
            Polygon.Screen(roiModel, snack, back = {
                roiModel.clearPoints()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.overview)
            }
        }

        // ROI overview
        b.composable(Routes.overview) {
            Overview.Screen(roiModel, activity.filesDir, snack) {
                roiModel.clear()
                nav.popClear(Routes.list)
            }
        }
    }
}