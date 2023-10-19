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
        const val list = "roi"
        const val name = "roi_name"
        const val contemporaryYears = "roi_cont_dates"
        const val contemporaryMonths = "roi_cont_months"
        const val historicalYears = "roi_hist_dates"
        const val historicalMonths = "roi_hist_months"
        const val polygon = "roi_polygon"
        const val overview = "roi_overview"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, activity: Activity, roiModel: RoiViewModel, analysisModel: AnalysisViewModel, appBar: AppBar, snack: SnackFun) {
        // ROI list
        b.composable(Routes.list) {
            RoiList.Screen(roiModel, object : RoiList.DirHolder {
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
            Name.Screen(roiModel, appBar, snack, back = {
                roiModel.clearName()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.contemporaryYears)
            }
        }

        // ROI contemporary years selection
        b.composable(Routes.contemporaryYears) {
            ContemporaryYears.Screen(roiModel, appBar, snack, back = {
                roiModel.clearContemporaryYears()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.contemporaryMonths)
            }
        }

        // ROI contemporary months selection
        b.composable(Routes.contemporaryMonths) {
            ContemporaryMonths.Screen(roiModel, appBar, back = {
                roiModel.clearContemporaryMonths()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.historicalYears)
            }
        }

        // ROI historical years selection
        b.composable(Routes.historicalYears) {
            HistoricalYears.Screen(roiModel, appBar, snack, back = {
                roiModel.clearHistoricalYears()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.historicalMonths)
            }
        }

        // ROI months range selection
        b.composable(Routes.historicalMonths) {
            HistoricalMonths.Screen(roiModel, appBar, back = {
                roiModel.clearHistoricalMonths()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.polygon)
            }
        }

        // ROI polygon creation
        b.composable(Routes.polygon) {
            CoarseRoi.Screen(roiModel, appBar, snack, back = {
                roiModel.drawPoly.clearAll()
                nav.popBackStack()
            }) {
                nav.navigate(Routes.overview)
            }
        }

        // ROI overview
        b.composable(Routes.overview) {
            Overview.Screen(roiModel, activity.filesDir, appBar, snack, nav::popBackStack) {
                roiModel.clear()
                nav.popClear(Routes.list)
            }
        }
    }
}