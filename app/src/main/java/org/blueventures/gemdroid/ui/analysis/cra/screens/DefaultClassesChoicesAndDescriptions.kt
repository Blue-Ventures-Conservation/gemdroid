package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.BVClass
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Butt
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.common.SnackFun

object DefaultClassesChoicesAndDescriptions {
    @Composable
    fun Screen(viewModel: CRAViewModel, snack: SnackFun, next: Click) {
        val bvClasses = viewModel.getDefaultBVClasses()

        val pairs = mutableListOf<Pair<BVClass, Boolean>>()

        bvClasses.forEach { item ->
            pairs.add(Pair(item, true))
        }

        val checkMap = remember { mutableStateMapOf(*pairs.toTypedArray()) }

        Col.MidPad(scroll = true) {
            Text(text = stringResource(R.string.select_which_of_our_recommended_classes_to_use), fontSize = 24.sp, textAlign = TextAlign.Center)
            Info.Block {
                for (bvClass in bvClasses) {
                    val localizedInfo = bvClass.localizedInfo(LocalContext.current)
                    var isExpanded by remember { mutableStateOf(false) }
                    Col.Col(bottom = 24.dp) {
                        Info.Row {
                            Checkbox(checkMap[bvClass]!!, onCheckedChange = { check ->
                                checkMap[bvClass] = check
                            })
                            Text(
                                localizedInfo.name,
                                Modifier.clickable { isExpanded = !isExpanded },
                            )
                            Box(modifier = Modifier
                                .background(bvClass.color)
                                .size(24.dp))
                        }
                        if (isExpanded) {
                            Col.Col(top = 8.dp, bottom = 8.dp, align = Alignment.Start) {
                                if (localizedInfo.subclasses.isEmpty()) {
                                    Text(localizedInfo.description)
                                } else {
                                    Text(stringResource(R.string.this_class_represents_the_following_subclasses), textAlign = TextAlign.Center)
                                    for (subclass in localizedInfo.subclasses) {
                                        Text(subclass.name, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, textAlign = TextAlign.Center)
                                        if (subclass.description != "") {
                                            Text(subclass.description)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Info.BlueLine()
                }
            }
            val min = 2
            val minMsg = stringResource(R.string.please_select_at_least).format(min.toString())
            val context = LocalContext.current.applicationContext
            Butt.Next {
                val checked = mutableListOf<BVClass>()
                for (bvClass in bvClasses) {
                    if (checkMap[bvClass] == true) {
                        checked.add(bvClass)
                    }
                }

                if (checked.size < min) {
                    snack(minMsg)
                } else {
                    viewModel.setCRAClasses(checked.map { it.toCRAClass(context) })
                    next()
                }
            }
        }
    }
}