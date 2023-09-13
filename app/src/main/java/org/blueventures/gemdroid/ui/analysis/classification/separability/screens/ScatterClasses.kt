package org.blueventures.gemdroid.ui.analysis.classification.separability.screens

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.classification.separability.JSONMap
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.ui.analysis.cra.CRA
import org.blueventures.gemdroid.ui.common.AppBarFun
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Effect
import org.blueventures.gemdroid.ui.common.GetRemote
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.Nav

object ScatterClasses {
    @Composable
    fun Screen(viewModel: SeparabilityViewModel, appBar: AppBarFun, back: Click, next: Click) {
        Nav.Wrap(back, next) { nav ->
            appBar(AppBarUpdate(viewModel.title))
            GetRemote.AwaitSave(viewModel::loadScatterFile, viewModel::getScatter, viewModel::saveScatterFile, errorHandler = CRA::errHandler) { json ->
                val classes = JSONMap.classes(json)

                if (classes == null) {
                    Effect.Once { nav.back() }
                    return@AwaitSave
                }

                CheckBoxes(viewModel, classes, nav::next)
            }
        }
    }

    @Composable
    fun CheckBoxes(viewModel: SeparabilityViewModel, classes: List<String>, next: Click) {
        val pairs = mutableListOf<Pair<String, Boolean>>()

        for (clz in classes) {
            pairs.add(Pair(clz, true))
        }

        val checkMap = remember { mutableStateMapOf(*pairs.toTypedArray()) }

        Col.MidPad(scroll = true) {
            Info.Header(stringResource(R.string.choose_classes))
            Info.Block {
                for (clz in classes) {
                    Info.Row {
                        Info.Txt("$clz:")
                        Checkbox(checkMap[clz]!!, onCheckedChange = { check ->
                            checkMap[clz] = check
                        })
                    }
                    Info.BlueLine()
                }
            }
            Butt.Next {
                val clzes = mutableListOf<String>()
                for (clz in classes) {
                    if (checkMap[clz] == true) {
                        clzes.add(clz)
                    }
                }
                viewModel.classes = clzes
                next()
            }
        }
    }
}