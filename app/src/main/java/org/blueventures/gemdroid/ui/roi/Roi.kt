package org.blueventures.gemdroid.ui.roi

import android.app.Activity
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.AnalysisViewModel
import org.blueventures.gemdroid.model.roi.RoiDatasource.Companion.maxExcludedRegions
import org.blueventures.gemdroid.model.roi.RoiViewModel
import org.blueventures.gemdroid.popClear
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.assess.Assess
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import org.blueventures.gemdroid.ui.common.maps.Maps
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygon
import org.blueventures.gemdroid.ui.common.polygons.CollectPolygons
import org.blueventures.gemdroid.ui.roi.screens.CoarsePolygonPurpose
import org.blueventures.gemdroid.ui.roi.screens.ContemporaryMonths
import org.blueventures.gemdroid.ui.roi.screens.ContemporaryYears
import org.blueventures.gemdroid.ui.roi.screens.CopiedPolygon
import org.blueventures.gemdroid.ui.roi.screens.HistoricalMonths
import org.blueventures.gemdroid.ui.roi.screens.HistoricalYears
import org.blueventures.gemdroid.ui.roi.screens.InlandMang
import org.blueventures.gemdroid.ui.roi.screens.Name
import org.blueventures.gemdroid.ui.roi.screens.Overview
import org.blueventures.gemdroid.ui.roi.screens.RoiList
import org.blueventures.gemdroid.ui.theme.MildRed
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
        const val copied_polygon = prefix + "copied_polygon"
        const val inland_mang = prefix + "inland_mang"
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
                viewModel.isAssessmentEditor = false
                nav.navigate(Routes.name)
            }
        }

        // ROI name creation
        b.backHandler(Routes.name, {
            viewModel.clearState()
            nav.popBackStack()
        }) {
            Name.Screen(viewModel, appBar, snack) {
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

        // ROI historical months selection
        b.backHandler(Routes.historicalMonths, {
            viewModel.clearHistoricalMonths()
            nav.popBackStack()
        }) {
            HistoricalMonths.Screen(viewModel, appBar, snack) {
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
            ContemporaryMonths.Screen(viewModel, appBar, snack) {
                if (viewModel.isAssessmentEditor) {
                    nav.navigate(Routes.overview)
                } else if (viewModel.importedROI.isEmpty()) {
                    nav.navigate(Routes.coarse_polygon_purpose)
                } else {
                    nav.navigate(Routes.prefix+CollectPolygons.Routes.option)
                }
            }
        }

        b.backHandler(Routes.coarse_polygon_purpose, nav::popBackStack) {
            CoarsePolygonPurpose.Screen(appBar) {
                var route = Routes.prefix+CollectPolygon.Routes.draw_or_shapefile
                if (viewModel.imported) {
                    route = Routes.copied_polygon
                }

                nav.navigate(route) {
                    popUpTo(Routes.contemporaryMonths)
                }
            }
        }

        b.backHandler(Routes.copied_polygon, nav::popBackStack) {
            CopiedPolygon.Screen(appBar, {
                nav.navigate(Routes.inland_mang) {
                    popUpTo(Routes.contemporaryMonths)
                }
            }) {
                nav.navigate(Routes.prefix+CollectPolygon.Routes.draw_or_shapefile) {
                    popUpTo(Routes.contemporaryMonths)
                }
            }
        }

        // Coarse ROI boundary creation
        CollectPolygon.screens(b, nav, Routes.prefix, Routes.inland_mang, appBar, snack, Maps.Storage.fromViewModel(viewModel), R.string.coarse_boundary, true, null, model = viewModel.coarseModel)

        b.backHandler(Routes.inland_mang, nav::popBackStack) {
            InlandMang.Screen(viewModel, appBar) {
                nav.navigate(Routes.prefix+CollectPolygons.Routes.option)
            }
        }

        // Sub-Regions
        CollectPolygons.screens(b, nav, Routes.prefix, Routes.prefix+CollectPolygon.Routes.draw_or_shapefile, Routes.overview, appBar, snack, Maps.Storage.fromViewModel(viewModel), R.string.excluded_region, R.string.excluded_regions, maxExcludedRegions, false, MildRed.toArgb(), model = viewModel)

        // ROI overview
        b.backHandler(Routes.overview, nav::popBackStack) {
            Overview.Screen(viewModel, activity.filesDir, appBar, snack) {
                viewModel.clearState()

                if (viewModel.isAssessmentEditor) {
                    nav.popClear(Assess.Routes.assess_description)
                } else {
                    nav.popClear(Routes.list)
                }
            }
        }
    }
}