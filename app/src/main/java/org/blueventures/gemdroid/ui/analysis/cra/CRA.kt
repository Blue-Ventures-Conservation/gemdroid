package org.blueventures.gemdroid.ui.analysis.cra

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.model.analysis.cra.HistoricalChoice
import org.blueventures.gemdroid.ui.analysis.Analysis
import org.blueventures.gemdroid.ui.analysis.cra.screens.CRAFields
import org.blueventures.gemdroid.ui.analysis.cra.screens.CheckForExistingCreation
import org.blueventures.gemdroid.ui.analysis.cra.screens.ChooseHistorical
import org.blueventures.gemdroid.ui.analysis.cra.screens.ContemporaryCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.CreateChosen
import org.blueventures.gemdroid.ui.analysis.cra.screens.CreateClasses
import org.blueventures.gemdroid.ui.analysis.cra.screens.CreateOrDefaultClasses
import org.blueventures.gemdroid.ui.analysis.cra.screens.Creation
import org.blueventures.gemdroid.ui.analysis.cra.screens.HistoricalCRA
import org.blueventures.gemdroid.ui.analysis.cra.screens.Purpose
import org.blueventures.gemdroid.ui.analysis.cra.screens.UploadChosen
import org.blueventures.gemdroid.ui.analysis.cra.screens.UploadCreation
import org.blueventures.gemdroid.ui.analysis.cra.screens.UploadOrCreate
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.backHandler
import java.net.HttpURLConnection

object CRA {
    object Routes {
        const val prefix = "analysis_cra_"
        const val purpose = prefix + "purpose"
        const val upload_or_create = prefix + "upload_or_create"
        const val create_chosen = prefix + "create_chosen"
        const val upload_chosen = prefix + "upload_chosen"
        const val check_for_existing_creation = prefix + "check_for_existing_creation"
        const val create_or_default_classes = prefix + "create_or_default_classes"
        const val create_classes = prefix + "create_classes"
        const val creation = prefix + "creation"
        const val upload_creation = prefix + "upload_creation"
        const val hist_choice = prefix + "hist_choice"
        const val hist_cra = prefix + "hist"
        const val cont_cra = prefix + "cont"
        const val cra_fields = prefix + "fields"
    }

    fun screens(b: NavGraphBuilder, nav: NavHostController, viewModel: CRAViewModel, appBar: AppBar, snack: SnackFun) {
        b.craBackHandler(Routes.purpose, appBar, nav::popBackStack) {
            Purpose.Screen {
                nav.navigate(Routes.upload_or_create)
            }
        }

        b.craBackHandler(Routes.upload_or_create, appBar, nav::popBackStack) {
            UploadOrCreate.Screen({
                nav.navigate(Routes.upload_chosen)
            }) {
                nav.navigate(Routes.create_chosen)
            }
        }

        b.craBackHandler(Routes.upload_chosen, appBar, nav::popBackStack) {
            UploadChosen.Screen {
                nav.navigate(Routes.hist_choice)
            }
        }

        b.craBackHandler(Routes.create_chosen, appBar, nav::popBackStack) {
            CreateChosen.Screen {
                nav.navigate(Routes.check_for_existing_creation)
            }
        }

        b.craBackHandler(Routes.check_for_existing_creation, appBar, nav::popBackStack) {
            CheckForExistingCreation.Screen(viewModel, {
                nav.navigate(Routes.creation)
            }) {
                nav.navigate(Routes.create_or_default_classes)
            }
        }

        b.craBackHandler(Routes.create_or_default_classes, appBar, {
            nav.popBackStack(Routes.create_chosen, false)
        }) {
            CreateOrDefaultClasses.Screen(viewModel, {
                nav.navigate(Routes.creation)
            }) {
                nav.navigate(Routes.create_classes)
            }
        }

        b.craBackHandler(Routes.create_classes, appBar, nav::popBackStack) {
            CreateClasses.Screen(viewModel) {
                nav.navigate(Routes.creation)
            }
        }

        b.craBackHandler(Routes.creation, appBar, {
            nav.popBackStack(Routes.create_chosen, false)
        }) {
            Creation.Screen(viewModel, appBar, snack) {
                nav.navigate(Routes.upload_creation)
            }
        }

        b.craBackHandler(Routes.upload_creation, appBar, nav::popBackStack) { back ->
            UploadCreation.Screen(viewModel, snack, back) {
                nav.popBackStack(Analysis.Routes.dashboard, false)
            }
        }

        b.craBackHandler(Routes.hist_choice, appBar, {
            viewModel.clearHistoricalChoice()
            nav.popBackStack()
        }) {
            ChooseHistorical.Screen(viewModel) {
                when (viewModel.historicalChoice) {
                    HistoricalChoice.SEPARATE -> {
                        nav.navigate(Routes.hist_cra)
                    }
                    else -> {
                        nav.navigate(Routes.cont_cra)
                    }
                }
            }
        }

        b.craBackHandler(Routes.hist_cra, appBar, nav::popBackStack) {
            HistoricalCRA.Screen(viewModel, snack) {
                nav.navigate(Routes.cont_cra)
            }
        }

        b.craBackHandler(Routes.cont_cra, appBar, nav::popBackStack) {
            ContemporaryCRA.Screen(viewModel, snack) {
                nav.navigate(Routes.cra_fields)
            }
        }

        b.craBackHandler(Routes.cra_fields, appBar, nav::popBackStack) {
            CRAFields.Screen(viewModel, snack, nav::popBackStack) {
                viewModel.clearState()
                nav.popBackStack(Analysis.Routes.dashboard, false)
            }
        }
    }

    fun NavGraphBuilder.craBackHandler(route: String, appBar: AppBar, back: Click, portrait: Boolean = false, content: @Composable (Click) -> Unit) {
        backHandler(route, back, portrait) { back ->
            appBar.Title(R.string.classification_reference_areas)
            content(back)
        }
    }

    fun errHandler(ctx: Context, code: Int?, message: String?): Pair<String?, Boolean> {
        return if (code == HttpURLConnection.HTTP_BAD_REQUEST && message?.contains("missing asset") == true) {
            Pair(ctx.getString(R.string.cra_not_found), false)
        } else {
            Pair(null, true)
        }
    }
}