package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.dynamics.DynamicsURLs
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Checklist
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.Progress
import org.blueventures.gemdroid.ui.common.SnackFun

object TargetClasses {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap(back) {
            appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))
            val classes = viewModel.cra.contemporaryCRA.stringClassValues
            val (classesChosen, setClassesChosen) = remember { mutableStateOf(false) }
            val (dynamicsResult, setDynamicsResult) = remember { mutableStateOf<Result<DynamicsURLs>?>(null) }

            when (classesChosen) {
                false -> {
                    Checklist.Screen(snack, stringResource(R.string.choose_class_dynamics), classes, initState = false, min = 1) { checked ->
                        viewModel.targetClasses = checked
                        setClassesChosen(true)
                    }
                }
                true -> {
                    when (dynamicsResult) {
                        null -> {
                            Progress()
                            viewModel.loadDynamicsFile(setDynamicsResult)
                        }
                        else -> {
                            Effect.Once {
                                viewModel.alreadyDownloaded = dynamicsResult.isSuccess
                                next()
                            }
                        }
                    }
                }
            }
        }
    }
}
